package uno.gpt.generators;

import uno.gpt.structure.Literal;

import java.util.HashMap;

abstract class AbstractGenerator implements GPTGenerator {
    /** Default values */
    static final int def_seed = 100, def_num_tree = 10;

    /** environment */
    HashMap<String, Literal> environment;

    /**
     * Helper function to find, copy, set and return a literal
     * @param id The Literal's id as a string
     * @param state The desired state
     * @return The Literal produced
     */
    Literal produceLiteral(String id, boolean state){
        // Find and copy
        Literal workingLiteral = environment.get(id).clone();
        // Set state
        workingLiteral.setState(state);
        // Return
        return workingLiteral;
    }

}
