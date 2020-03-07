package ru.ifmo.rain.vlasova.implementor;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Objects;

public class ComparedMethod {
    private Method method;

    ComparedMethod(Method method) {
        this.method = method;
    }

    Method getMethod() {
        return method;
    }

    public int hashCode() {
        int POW = 31;
        int MOD = 1000000007;
        int hash = (method.getName().hashCode() % MOD + method.getReturnType().hashCode() % MOD) % MOD;
        hash = (hash + POW * Arrays.hashCode(method.getParameterTypes())) % MOD;
        return hash;
    }

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
