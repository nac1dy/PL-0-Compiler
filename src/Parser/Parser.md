-------------------- 

#### Dieses File soll die Kommentare ein wenig ersetzten im Parser!

--------------------

Der Parser ist sehr wichtig und wird in dieser Dokumentation ein wenig mehr Zeit in Anspruch nehmen.
Grundlegend kann man sagen, der Parser ist der Punkt wo aus der Liste von Tokens, ein Abstrakter Syntax Baum wird
mit welchem die Symbol Tabelle und der Code Generator dann was anfangen kann.

Man könnte auch den Parser darauf reduzieren, dass er einfach nur über die Liste iteriert und je nach welches Token er matched
erstellt er einen neuen Knoten im Baum. (Theoretische Informatik Grammatiken für mehr Informationen (ich meine damit nicht das Modul!))
Jedoch wäre das ein wenig zu wenig, würde man das einfach so bauen, würde man die Assoziativität außer acht lassen
Der Baum ist nicht mehr eindeutig
Der Baum würde auch die precedence nicht beachten von Operatoren

Deshalb ist es wichtig, sich an der Grammatik der Sprache zu orientieren, und diese in Form von Funktionen zu realisieren
Damit ermöglicht man, das diese Fehler behoben werden.

Dementsprechend orientiert sich dieser Parser an folgender Grammatik:

````PL/0
program = block "." ;

block = [ "const" ident "=" number {"," ident "=" number} ";"]
        [ "var" ident {"," ident} ";"]
        { "procedure" ident ";" block ";" } statement ;

statement = [ ident ":=" expression | "call" ident 
              | "?" ident | "!" expression 
              | "begin" statement {";" statement } "end" 
              | "if" condition "then" statement 
              | "while" condition "do" statement ];

condition = "odd" expression |
            expression ("="|"#"|"<"|"<="|">"|">=") expression ;

expression = [ "+"|"-"] term { ("+"|"-") term};

term = factor {("*"|"/") factor};

factor = ident | number | "(" expression ")";
````
Zu finden: https://en.wikipedia.org/wiki/PL/0

Und genau diese Grammatik Struktur wird man immer wieder in diesem Code wieder finden. Sie ist elementar für den Aufbau eines sauberen Parsers

Außerdem geht dieser Parser Hand in Hand mit dem Visitor Pattern.

**Mehr dazu in [VisitorPattern.md](VisitorPattern.md)**

Auch für den Parser wurde folgendes File erstellt

**[GenerateAst.java](../AST/GenerateAst.java)**

Es dient zur einfachen erstellung von Typen für den AST
- Expressions (unterteilt in Binary, Grouping, Literal, Unary und Variable)
- Condition (unterteilt in Binary und Unary)
- Statement (unterteilt in OutputStmt, IfStmt, WhileStmt, RepeatUntilStmt, InputStmt, AssignStmt, CallStmt und BeginEndStmt)

Es werden zudem noch die Klasse Programm und Block erstellt jedoch sind die Manuell erstellt da diese PL/0 spezifisch sind und das Visitor Pattern hier
an der Implementation aus Lox orientiert.

All diese werden zu Klassen welche wiederum das Visitor Pattern alle Erben
Dadurch kann ich am ende einfach sagen ich erstelle eine neue Binary Expression mit den Argumenten 
- Linke Expression
- Operator
- Rechte Expression

Und der Visitor kann später sagen visitBinaryExpression und so erstelle ich eine allgemeingültige Methode visit mit dem Interface Visitor
und kann ohne Probleme den Baum "Traversieren" ("The pattern isn’t about “visiting”, and the “accept” method in it doesn’t conjure up any helpful imagery either" ~Robin Nyrstrom)

**https://craftinginterpreters.com/representing-code.html#implementing-syntax-trees (Chapter 5.3.2)** 

Vorgeplänkel Ende.


# Implementierung

Man startet, wie in der Grammatik, damit einen Block zu Parsen in dem ich eine neue Instanz eines Blocks erstelle
Dann wird "consume" (helper der aktuelles Token mit übergebenem Überprüft und Error mit übergebener Message wirft wenn was schiefläuft)
aufgerufen um auf den Punkt am ende des Blockes zu Prüfen und wenn dieser Existiert, ist das Programm zu ende also wird noch auf EOF geprüft und ganz am ende wird das Programm mit dem Block als Parameter zurückgegeben

Das ist der Start vom Parser und genauso der Start der Grammatik

````Java
    public Program parseProgram() {
    try {
        Block block = block();
        consume(TokenType.DOT, "Expect '.' after program.");
        consume(TokenType.EOF, "Expect end of file after '.'. Found: " + peek().type + " ('" + peek().lexeme + "')");
        return new Program(block);
    } catch (ParseError e) {
        return null;
    }
}
````

Wie man sieht, folgt dann die "block()" Funktion

Diese matched alle Deklarationen von Konstanten, Variablen und Prozeduren und speichert diese Deklarationen in einer Liste

Dafür wurden dementsprechend wieder Klassen implementiert
1. ConstDecl
2. VarDecl
3. ProcDecl

Und in Block werden all diese in der jeweiligen Liste gespeichert.

Je nach dem welchen Token gematched wird:
````Java
    private Block block() {

    /*
     * First we match every declaration of consts and vars
     */
    List<ConstDecl> constDecls = new ArrayList<>();
    List<VarDecl> varDecls = new ArrayList<>();
    List<ProcDecl> procDecls = new ArrayList<>();

    // const ... ;
    try {
        if (match(TokenType.CONST)) {
            do {
                Token name = consume(TokenType.IDENTIFIER, "Expect constant name.");
                consume(TokenType.EQUAL, "Expect '=' after constant name.");
                Token valueTok = consume(TokenType.NUMBER, "Expect number after '=' in const declaration.");
                constDecls.add(new ConstDecl(name, (Integer) valueTok.literal));
            } while (match(TokenType.COMMA));
            consume(TokenType.SEMICOLON, "Expect ';' after const declaration.");
        }


        // var ... ;
        if (match(TokenType.VAR)) {
            do {
                Token name = consume(TokenType.IDENTIFIER, "Expect variable name.");
                varDecls.add(new VarDecl(name));
            } while (match(TokenType.COMMA));
            consume(TokenType.SEMICOLON, "Expect ';' after var declaration.");
        }

        // procedure ... ; block ;
        /*
         * once we hit procedures, we call a new Block and start again.
         */
        while (match(TokenType.PROCEDURE)) {
            Token name = consume(TokenType.IDENTIFIER, "Expect procedure name.");
            consume(TokenType.SEMICOLON, "Expect ';' after procedure name.");
            Block body = block();
            consume(TokenType.SEMICOLON, "Expect ';' after procedure block.");
            procDecls.add(new ProcDecl(name, body));
        }
    } catch (ParseError e) {
        synchronize();
    }

    Stmt statement = statement();
    return new Block(constDecls, varDecls, procDecls, statement);
}
````
Wir speichern also alle declarationen in der entsprechenden Liste und rufen zu, schluss die statement Funktion auf.
Dann returnen wir den Block den wir gerade geparsed haben.


Jetzt können wir weiter machen mit der richtigen verarbeitung von Statements.


Die statement() methode durchläuft die Token Liste und schaut in einem großen If-Statement auf welchem Statement man gerade ist.
Und je nach dem wird dann eine neue Node erstellt.

Und das tolle daran ist, das kann man genauso für Conditions und Expression bauen, das heißt, die Funktionen 
condition() und expression() funktionieren genauso, nur das expression und condition nochmal unterteilt sind in 
Binary, Unary usw. Bei Statements ist so eine unterteilung ein wenig, "unnötig". 

Interessant ist aber dennoch das man das bei Expressions und Conditions so macht weil die Grammatik so aufgebaut ist.
Wenn man sich die Grammatik von PL/0 anschaut, wird condition weniger aber expression sehr zerlegt in 
expression - term - factor. 
Das wird gemacht um linksrekursion zu vermeiden und mehrdeutigkeit zu verhindern.

So, das war die Theorie, es gibt in dieser Implementation natürlich wieder viele kleine Helper welche aber relativ selbsterklärend sind.

Das letzte interessante wird die synchronize() Methode sein. Sie ist dazu da, den Parser bei einem Error zu helfen wieder auf die richtige
Spur zu kommen. Es werden einfach Statements ignoriert bis ein Statement Ende, ";", kommt.

Die Idee ist das verhindern von "Cascade Errors", wenn wir ein Syntax Error beim Parsen finden, wird dementsprechend alles folgende auch ein Error sein, 
also verwerfen wir diese "Fehlerhaften" Statements um wieder in Sync zu kommen mit den "Richtigen". 

Und that's it eigentlich.


Wir haben also aus einer Liste an Tokens die der Lexer vorbereitet hat, einen Baum generiert mit verschiedenen Nodes und Blättern und diesen werden wir jetzt mit einem Inspector durchsuchen und die wichtigen Informationen speichern.


