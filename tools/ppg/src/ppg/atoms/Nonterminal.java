package jltools.util.jlgen.atoms;

import java.util.*;
import jltools.util.jlgen.util.*;

public class Nonterminal extends GrammarSymbol
{
	private String name, label;
	
	public Nonterminal(String name, String label)
	{
		this.name = name;
		this.label = label;
	}

	public Nonterminal(String name)
	{
		this.name = name;
		label = null;
	}

	public boolean equals(Object o) {
		if (o instanceof Nonterminal) {
			return name.equals( ((Nonterminal)o).getName() );	
		}
		return false;
	}
		public String toString() {
		String result = name;		if (label != null)			result += ":" + label;		return result;
	}	
	public String getName() {
		return name;	
	}
}
