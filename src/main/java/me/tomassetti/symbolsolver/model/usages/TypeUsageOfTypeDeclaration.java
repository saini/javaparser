package me.tomassetti.symbolsolver.model.usages;

import me.tomassetti.symbolsolver.model.*;
import me.tomassetti.symbolsolver.model.declarations.MethodDeclaration;
import me.tomassetti.symbolsolver.model.declarations.TypeDeclaration;
import me.tomassetti.symbolsolver.model.javaparser.declarations.JavaParserTypeVariableDeclaration;
import me.tomassetti.symbolsolver.model.usages.TypeUsage;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Created by federico on 31/07/15.
 */
public class TypeUsageOfTypeDeclaration implements TypeUsage {

    private TypeDeclaration typeDeclaration;
    private List<TypeUsage> typeParameters;

    public TypeUsageOfTypeDeclaration(TypeDeclaration typeDeclaration) {
        this(typeDeclaration, Collections.emptyList());
    }

    public TypeUsageOfTypeDeclaration(TypeDeclaration typeDeclaration, List<TypeUsage> typeParameters) {
        this.typeDeclaration = typeDeclaration;
        this.typeParameters = typeParameters;
    }

    @Override
    public boolean isArray() {
        return false;
    }

    @Override
    public boolean isPrimitive() {
        return false;
    }

    @Override
    public boolean isReferenceType() {
        return true;
    }

    @Override
    public String toString() {
        return "TypeUsageOfTypeDeclaration{" +
                "typeDeclaration=" + typeDeclaration +
                ", typeParameters=" + typeParameters +
                '}';
    }

    private Optional<TypeUsage> typeParamByName(String name){
        int i =  0;
        for (TypeParameter tp : typeDeclaration.getTypeParameters()){
            if (tp.getName().equals(name)) {
                return Optional.of(this.typeParameters.get(i));
            }
            i++;
        }
        return Optional.empty();
    }

    @Override
    public Optional<Value> getField(String name, TypeSolver typeSolver) {
        if (!typeDeclaration.hasField(name)){
            return Optional.empty();
        }
        TypeDeclaration typeOfField = typeDeclaration.getField(name).getType(typeSolver);
        TypeUsage typeUsage = new TypeUsageOfTypeDeclaration(typeOfField);

        //ora io dovrei capire che mi ha restituito una variabile che si riferisce alla classe
        //rappresentata da THIS. Per capirlo potremmo associare piu' info alle TypeVariable,
        //mettendo dove sono state dichiarate

        if (typeUsage.isTypeVariable()) {
            TypeParameter typeParameter = typeUsage.asTypeParameter();
            if (typeParameter.declaredOnClass()) {
                Optional<TypeUsage> typeParam = typeParamByName(typeParameter.getName());
                if (typeParam.isPresent()) {
                    typeUsage = typeParam.get();
                }
            }
        }

        return Optional.of(new Value(typeUsage, name, true));
    }

    @Override
    public String getTypeName() {
        return typeDeclaration.getQualifiedName();
    }

    @Override
    public TypeUsage getBaseType() {
        throw new UnsupportedOperationException();
    }

    @Override
    public Context getContext() {
        throw new UnsupportedOperationException();
    }

    @Override
    public SymbolReference<MethodDeclaration> solveMethod(String name, List<TypeUsage> parameterTypes, TypeSolver typeSolver) {
        return typeDeclaration.solveMethod(name, parameterTypes, typeSolver);
    }

    @Override
    public List<TypeUsage> parameters() {
        return typeParameters;
    }

    @Override
    public TypeParameter asTypeParameter() {
        if (this.typeDeclaration instanceof JavaParserTypeVariableDeclaration){
            JavaParserTypeVariableDeclaration javaParserTypeVariableDeclaration = (JavaParserTypeVariableDeclaration)this.typeDeclaration;
            return javaParserTypeVariableDeclaration.asTypeParameter();
        }
        throw new UnsupportedOperationException(this.typeDeclaration.getClass().getCanonicalName());
    }

    @Override
    public boolean isTypeVariable() {
        return typeDeclaration.isTypeVariable();
    }

    public boolean isFunctionOrPredicate() {
        return false;
    }
}
