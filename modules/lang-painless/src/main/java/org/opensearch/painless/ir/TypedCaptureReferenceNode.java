/*
 * Licensed to Elasticsearch under one or more contributor
 * license agreements. See the NOTICE file distributed with
 * this work for additional information regarding copyright
 * ownership. Elasticsearch licenses this file to you under
 * the Apache License, Version 2.0 (the "License"); you may
 * not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.opensearch.painless.ir;

import org.opensearch.painless.ClassWriter;
import org.opensearch.painless.DefBootstrap;
import org.opensearch.painless.Location;
import org.opensearch.painless.MethodWriter;
import org.opensearch.painless.phase.IRTreeVisitor;
import org.opensearch.painless.symbol.WriteScope;
import org.opensearch.painless.symbol.WriteScope.Variable;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;

public class TypedCaptureReferenceNode extends ReferenceNode {

    /* ---- begin node data ---- */

    private String methodName;

    public void setMethodName(String methodName) {
        this.methodName = methodName;
    }

    public String getMethodName() {
        return methodName;
    }

    /* ---- end node data, begin visitor ---- */

    @Override
    public <Scope> void visit(IRTreeVisitor<Scope> irTreeVisitor, Scope scope) {
        irTreeVisitor.visitTypeCaptureReference(this, scope);
    }

    @Override
    public <Scope> void visitChildren(IRTreeVisitor<Scope> irTreeVisitor, Scope scope) {
        // do nothing; terminal node
    }

    /* ---- end visitor ---- */

    public TypedCaptureReferenceNode(Location location) {
        super(location);
    }

    @Override
    protected void write(ClassWriter classWriter, MethodWriter methodWriter, WriteScope writeScope) {
        methodWriter.writeDebugInfo(getLocation());
        Variable captured = writeScope.getVariable(getCaptures().get(0));

        methodWriter.visitVarInsn(captured.getAsmType().getOpcode(Opcodes.ILOAD), captured.getSlot());
        Type methodType = Type.getMethodType(MethodWriter.getType(getExpressionType()), captured.getAsmType());
        methodWriter.invokeDefCall(methodName, methodType, DefBootstrap.REFERENCE, getExpressionCanonicalTypeName());
    }
}