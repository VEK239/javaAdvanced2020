package ru.ifmo.rain.vlasova.implementor;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Objects;


/**
 * Class helping {@link ImplementorCodeGenerator} to make all methods unique
 *
 * @author Elizaveta Vlasova (vlasova.elizaveta@yandex.ru)
 */
public class ComparedMethod {
    /*
     * The method to be hashed and compared
     */
    private Method method;

    /**
     * The constructor crating the new instance of ComparedMethod
     *
     * @param method the simple method to be made unique
     */
    ComparedMethod(Method method) {
        this.method = method;
    }

    /**
     * A method to return the current method
     *
     * @return the method itself
     */
    Method getMethod() {
        return method;
    }

    /**
     * A method counting the hashcode of the current method
     *
     * @return the hash of the method
     */
    public int hashCode() {
        int POW = 31;
        int MOD = 1000000007;
        int hash = (method.getName().hashCode() % MOD + method.getReturnType().hashCode() % MOD) % MOD;
        hash = (hash + POW * Arrays.hashCode(method.getParameterTypes())) % MOD;
        return hash;
    }

    /**
     * A method checking whether the object that is given and the method are the same
     *
     * @param o an object for this method to be compared to
     * @return true if equal, false otherwise
     */
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        ComparedMethod that = (ComparedMethod) o;
        return Objects.equals(method.getReturnType(), that.method.getReturnType()) &&
                method.getName().equals(that.method.getName()) &&
                Arrays.equals(method.getParameterTypes(), that.method.getParameterTypes());
    }
}
