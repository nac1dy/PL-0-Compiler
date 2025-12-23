# VisitorPattern 

Das Problem was versucht wird zu lösen ist folgendes:

Hätten wir eine Tabelle mit

Spalten : Methoden 
Zeilen : Klassen.

Wöllten wir eine Spalte hinzufügen, müssten alle Methoden für diese neue Klasse definiert werden

Wöllten wir eine Methode hinzufügen, müssten alle Klassen diese neue Methode implementieren.

***Das ist ziemlich umständlich und wir von verschiedenen Programmiersprachen besser/schlechter gelöst.***

## Was macht jetzt das Visitor Pattern um das zu lösen?

Sei folgendes gegeben:

```java
abstract class Pastry {
}

class Beignet extends Pastry {
}

class Cruller extends Pastry {
}
```

Wir wollen jetzt neue Methoden der Pasty Klasse hinzufügen ohne diese neue Methode in jeder Klasse zu implementieren.

Also definieren wir eine Visitor Schnittstelle/ein Visitor Interface:

```java  
interface PastryVisitor {
    void visitBeignet(Beignet beignet); 
    void visitCruller(Cruller cruller);
}
```

Jede neue Methode die wir neu hinzufügen, wird als Klasse dargestellt welche das Interface implementiert.
Und dieses besagt Interface hat für jeden Typen von Pasty eine visit Methode.

Sei nun eine Pasty gegeben, wie finden wir die richtige Methode raus, welche zu dieser Pasty gehört?

Wir fügen der Pasty Klasse eine accept Methode hinzu:

```java
abstract class Pastry {
    abstract void accept(PastryVisitor visitor);
}
```
Jede konkrete Pasty Klasse implementiert diese accept Methode:

```java
class Beignet extends Pastry {
    void accept(PastryVisitor visitor) {
        visitor.visitBeignet(this);
    }
}
class Cruller extends Pastry {
    void accept(PastryVisitor visitor) {
        visitor.visitCruller(this);
    }
}
```

Um jetzt eine Methode auf eine Pastry anzuwenden:
1. rufen wir die accept() methode
2. übergeben den Visitor für die Methode die wir ausführen wollen
3. Die Pastry ruft die accept Methode auf und übergibt sich selbst an die richtige visit Methode des Visitors.

In Pseudocode:

```java
Pastry pastry = new Beignet();
PastryVisitor visitor = new Visitor();
pastry.accept(visitor);
```
Hier wird auch folgendes gemacht:
1. Pastry wird als Beignet instanziert
2. Visitor wird instanziert
3. Der Visitor "besucht" die Pastry
4. Die accept Methode des Beignets wird aufgerufen
5. die accept methode wiederum ruft die visitBeignet Methode des Visitors auf und übergibt die Beignet Instanz (sich selbst/"this").


Das klingt sehr verwirrend, einerseits gibt es eine Klasse Pastry welche verschiedene Gebäcke symbolisiert.
Dann gibt es ein Visitor Interface mit visit Methoden für jedes Gebäck.
Und jetzt soll es noch methoden geben, welche von der Pastry Klasse ausgeführt werden und methoden des Inerfaces aufrufen.

Und all das, um neue Methoden hinzuzufügen ohne die Pastry Klassen zu verändern

# Zusammenfasssend

Das Visitor Pattern ermöglicht es, neue Operationen auf einer Gruppe von Objekten durchzuführen, ohne die Klassen dieser Objekte zu verändern. 
Stattdessen wird eine separate Visitor-Klasse erstellt, die die neuen Operationen implementiert. 
Die Objekte akzeptieren den Visitor und delegieren die Ausführung der Operation an die entsprechende Methode des Visitors.


Für mehr Informationen dazu:
https://craftinginterpreters.com/representing-code.html Chapter 5.3.2

https://refactoring.guru/design-patterns/visitor 

