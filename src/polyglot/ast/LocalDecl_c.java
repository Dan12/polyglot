package jltools.ext.jl.ast;

import jltools.ast.*;
import jltools.types.*;
import jltools.visit.*;
import jltools.util.*;

/** 
 * A <code>LocalDecl</code> is an immutable representation 
 * of a variable declaration, which consists of a type, one or more variable
 * names, and possible initilization expressions.
 */
public class LocalDecl_c extends Stmt_c implements LocalDecl
{
    Declarator_c decl;
    LocalInstance li;

    public LocalDecl_c(Ext ext, Position pos, Flags flags, TypeNode type, String name, Expr init) {
        super(ext, pos);
	this.decl = new Declarator_c(flags, type, name, init);
    }

    public Type declType() {
        return decl.declType();
    }

    public Flags flags() {
        return decl.flags();
    }

    public LocalDecl flags(Flags flags) {
        LocalDecl_c n = (LocalDecl_c) copy();
	n.decl = decl.flags(flags);
	return n;
    }

    public TypeNode type() {
        return decl.type();
    }

    public LocalDecl type(TypeNode type) {
        LocalDecl_c n = (LocalDecl_c) copy();
	n.decl = decl.type(type);
	return n;
    }

    public String name() {
        return decl.name();
    }

    public LocalDecl name(String name) {
        LocalDecl_c n = (LocalDecl_c) copy();
	n.decl = decl.name(name);
	return n;
    }

    public Expr init() {
        return decl.init();
    }

    public LocalDecl init(Expr init) {
        LocalDecl_c n = (LocalDecl_c) copy();
	n.decl = decl.init(init);
	return n;
    }

    public LocalInstance localInstance() {
        return li;
    }

    public LocalDecl localInstance(LocalInstance li) {
        LocalDecl_c n = (LocalDecl_c) copy();
	n.li = li;
	return n;
    }

    protected LocalDecl_c reconstruct(TypeNode type, Expr init) {
        if (type() != type || init() != init) {
	    LocalDecl_c n = (LocalDecl_c) copy();
	    n.decl = (Declarator_c) decl.copy();
	    n.decl = n.decl.type(type);
	    n.decl = n.decl.init(init);
	    return n;
	}

	return this;
    }

    public Node visitChildren(NodeVisitor v) {
        TypeNode type = (TypeNode) type().visit(v);

	Expr init = null;

	if (init() != null) {
	    init = (Expr) init().visit(v);
	}

	return reconstruct(type, init);
    }

    // Add the variable to the scope _after_ the declaration.
    public void leaveScope(Context c) {
        c.addVariable(li);
    }

    public Node buildTypes_(TypeBuilder tb) throws SemanticException {
	TypeSystem ts = tb.typeSystem();

	LocalInstance li = ts.localInstance(position(),
	    				    flags(), declType(), name());

	if (init() instanceof Lit && flags().isFinal()) {
	    Object value = ((Lit) init()).objValue();
	    li = (LocalInstance) li.constantValue(value);
	}

	return localInstance(li);
    }

    // Override so we can do this test before we enter scope.
    // Return null to let the traversal continue.
    public Node typeCheckOverride_(TypeChecker tc) throws SemanticException {
        Context c = tc.context();

	try {
	    c.findLocal(li.name());

	    throw new SemanticException(
		"Local variable " + li + " multiply-defined in " +
		c.currentCode() + ".");
	}
	catch (SemanticException e) {
	}

	return null;
    }

    public Node typeCheck_(TypeChecker tc) throws SemanticException {
	TypeSystem ts = tc.typeSystem();

	try {
	    ts.checkLocalFlags(flags());
	}
	catch (SemanticException e) {
	    throw new SemanticException(e.getMessage(), position());
	}

	decl.typeCheck(tc);

	return this;
    }

    public String toString() {
	return decl.toString();
    }

    public void translate_(CodeWriter w, Translator tr) {
        boolean semi = tr.appendSemicolon(true);

	decl.translate(w, tr, false);

	if (semi) {
	    w.write(";");
	}

	tr.appendSemicolon(semi);
    }

    public void dump(CodeWriter w) {
	super.dump(w);

	if (li != null) {
	    w.allowBreak(4, " ");
	    w.begin(0);
	    w.write("(instance " + li + ")");
	    w.end();
	}
    }

    public Node reconstructTypes_(NodeFactory nf, TypeSystem ts, Context c)
        throws SemanticException {

	Flags flags = flags();
        Type type = declType();
        String name = name();
	Expr init = init();

	LocalInstance li = this.li;

	if (! flags.equals(li.flags())) li = li.flags(flags);
	if (! type.equals(li.type())) li = li.type(type);
	if (! name.equals(li.name())) li = li.name(name);

	if (init instanceof Lit && flags.isFinal()) {
	    Object value = ((Lit) init).objValue();

	    if (value != li.constantValue()) {
		li = (LocalInstance) li.constantValue(value);
	    }
	}
	else if (li.isConstant()) {
	    li = (LocalInstance) li.constantValue(null);
	}

	if (li != this.li) {
	    return localInstance(li);
	}

	return this;
    }
}
