# Notes for the Parser


Important for this Chapter is Precedence and Associativity

Precedence: Which operator is evaluated first in an expression with multiple operators.
/ and * have higher precedence than + and -. Which also means they bind more tightly.

Associativity: The order in which operators of the same precedence are evaluated (left-to-right or right-to-left).

| Name   | Operators | Associates |
|--------|-----------|------------|
| Term   | -, +      | Left       |
| Factor | /, *      | Left       |
| Unary  | -         | Right      |

Because the current Grammar of Expressions is ambiguous, we need to refactor it to take Precedence and Associativity into account.

```
expression     → term ;

term           → factor ( ( "-" | "+" ) factor )* ;
factor         → unary ( ( "/" | "*" ) unary )* ;
unary          → ( "-" | odd) unary | primary ;
primary        → NUMBER | IDENT |  "(" expression ")" ;
```

This is the new Grammar, which takes Precedence and Associativity into account and goes from least (equality) to most (primary) precedence.

The Parser will use Recursive Descent Parsing

| Grammar notation | Code representation                |
|------------------|------------------------------------|
| Terminal         | Code to match and consume a token; |
| Nonterminal      | Call to that rule’s function;      |
| \|               | if or switch statement;            |
| * or +           | while or for loop;                 |
| ?                | if statement;                      |
