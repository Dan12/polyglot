/*
 * ParseFile.java
 * 
 * Author: nystrom
 * Creation date: Dec 19, 2004
 */
package polyglot.frontend.goals;

import polyglot.frontend.*;



public class Parsed extends SourceFileGoal {
    public Parsed(Job job) { super(job); }
    
    public Pass createPass(ExtensionInfo extInfo) {
        return new ParserPass(extInfo.compiler(), this);
    }
    
    public int distanceFromGoal() {
        return job().ast() != null ? 0 : 1;
    }
}