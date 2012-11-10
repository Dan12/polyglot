/*******************************************************************************
 * This file is part of the Polyglot extensible compiler framework.
 *
 * Copyright (c) 2000-2012 Polyglot project group, Cornell University
 * Copyright (c) 2006-2012 IBM Corporation
 * All rights reserved.
 *
 * This program and the accompanying materials are made available under
 * the terms of the Eclipse Public License v1.0 which accompanies this
 * distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * This program and the accompanying materials are made available under
 * the terms of the Lesser GNU Public License v2.0 which accompanies this
 * distribution.
 * 
 * The development of the Polyglot project has been supported by a
 * number of funding sources, including DARPA Contract F30602-99-1-0533,
 * monitored by USAF Rome Laboratory, ONR Grants N00014-01-1-0968 and
 * N00014-09-1-0652, NSF Grants CNS-0208642, CNS-0430161, CCF-0133302,
 * and CCF-1054172, AFRL Contract FA8650-10-C-7022, an Alfred P. Sloan 
 * Research Fellowship, and an Intel Research Ph.D. Fellowship.
 *
 * See README for contributors.
 ******************************************************************************/
package polyglot.ext.jl5.ast;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import polyglot.ast.ClassBody_c;
import polyglot.ast.ClassMember;
import polyglot.util.CodeWriter;
import polyglot.util.Position;
import polyglot.util.SerialVersionUID;
import polyglot.visit.PrettyPrinter;

public class JL5ClassBody_c extends ClassBody_c {
    private static final long serialVersionUID = SerialVersionUID.generate();

    public JL5ClassBody_c(Position pos, List<ClassMember> members) {
        super(pos, members);
    }

    @Override
    public void prettyPrint(CodeWriter w, PrettyPrinter tr) {
        // check if we have any EnumConstantDecl
        List<EnumConstantDecl> ecds = enumConstantDecls();
        if (ecds.isEmpty()) {
            super.prettyPrint(w, tr);
            return;
        }

        if (!members.isEmpty()) {
            w.newline(4);
            w.begin(0);
            ClassMember prev = null;

            for (Iterator<EnumConstantDecl> i = ecds.iterator(); i.hasNext();) {
                EnumConstantDecl ecd = i.next();
                prev = ecd;
                print(ecd, w, tr);
                w.write(i.hasNext() ? "," : ";");
                w.allowBreak(1);
            }
            if (!ecds.isEmpty()) {
                w.newline(0);
            }

            for (Iterator<ClassMember> i = members.iterator(); i.hasNext();) {
                ClassMember member = i.next();
                if (member instanceof EnumConstantDecl) {
                    // already printed it
                    continue;
                }

                if ((member instanceof polyglot.ast.CodeDecl)
                        || (prev instanceof polyglot.ast.CodeDecl)) {
                    w.newline(0);
                }
                prev = member;
                printBlock(member, w, tr);
                if (i.hasNext()) {
                    w.newline(0);
                }
            }

            w.end();
            w.newline(0);
        }
    }

    protected List<EnumConstantDecl> enumConstantDecls() {
        List<EnumConstantDecl> ecds = new ArrayList<EnumConstantDecl>();
        for (ClassMember m : this.members) {
            if (m instanceof EnumConstantDecl) {
                ecds.add((EnumConstantDecl) m);
            }
        }
        return ecds;
    }

}
