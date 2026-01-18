-------------------- 

#### Dieses File soll die Kommentare ein wenig ersetzten im Lexer!

--------------------

# Phase 1

Den Lexer/Scanner kann man als Phase 1 des Compiling bezeichnen, er nimmt den Code in Textform als Input
und erstellt daraus eine Liste an Tokens, diese kann der Parser dann verarbeiten. 

Somit lässt sich ein Source Code in Tokens kategorisieren und man kann später leichter danach Matchen und arbeitet Effizienter
als auf Strings


## Ablauf

Eine komplett detaillierte Ablauf beschreibung braucht es gar nicht um zu verstehen was passiert.
Stell dir einfach vor du hast folgenden Code:

````Pseudo
var x;
begin
  x := 5;
  ! x
end.
````

Dann erhält der Lexer diesen Code als String als sein Input und geht in einer while-Schleife durch den Input durch.
Dabei wird eine Variable current geführt welche speichert an welcher Stelle wir im code sind um 
1. Eine abbruch Bedingung für die While-Schleife zu haben
2. Den aktuellen Teil des Codes ordentlich matchen zu können

Während dieser while-Schleife wird dann mittels Switch-Case und einer einfachen Match methode geschaut
was das aktuelle Token ist und je nach dem wird dann ein neuer Token erstellt und in eine Liste von Tokens eingefügt

Es werden noch viele weitere kleinere Helfer benutzt
1. eine prüfung auf ob Alphabetisch oder numerischer Value ist
2. Peek() um den aktuellen Charakter auf dem wir stehen (laut current) zurückzugeben
3. advance() um den nächsten Charakter im Code zurückzugeben

Je nach dem auf welchem Charakter wir gerade stehen generieren wird ein Token und legen das in eine Token Liste
diese wird dann dem Parser als Input gegeben.
