package Types;

/**
 * Root-Knoten der PL/0-Quelle.
 * Grammatik:
 *   program -> block "." ;
 */
public class Program {

    public final Block block;

    public Program(Block block) {
        this.block = block;
    }
}
