package dev.zhiqin.sqlcommand.annotation;

import android.database.sqlite.SQLiteDatabase;
import android.util.SparseIntArray;

import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.Collection;
import java.util.Iterator;

import dev.zhiqin.sqlcommand.SQL;
import dev.zhiqin.sqlcommand.command.Insert;
import dev.zhiqin.sqlcommand.command.Select;
import rx.Observable;
import rx.Subscriber;

public class SqlBuilder {

    public static <T> T build(SQLiteDatabase database, Class<T> clazz) {
        validateServiceClass(clazz);
        return (T) Proxy.newProxyInstance(clazz.getClassLoader(), new Class<?>[]{clazz},
                new SqlProxy<T>(database));
    }

    static <T> void validateServiceClass(Class<T> service) {
        if (!service.isInterface()) {
            throw new IllegalArgumentException("Only support for interface.");
        }
        if (service.getInterfaces().length > 0) {
            throw new IllegalArgumentException("Interface definitions must not extend other interfaces.");
        }
    }

    static class SqlProxy<T> implements InvocationHandler {
        SQLiteDatabase db;

        public SqlProxy(SQLiteDatabase db) {
            this.db = db;
        }

        @Override
        public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
            if (method.getDeclaringClass() == Object.class) {
                return method.invoke(this, args);
            }

            MethodInfo methodInfo = MethodInfo.process(method);
            boolean hasWhere = !"".equals(methodInfo.where);

            switch (methodInfo.sqlType) {
                case MethodInfo.SQL_TYPE_SELECT:
                    Select<?> select = SQL.with(db).select(methodInfo.tableClass);
                    if (hasWhere) {
                        return methodInfo.generateReturn(select.where(methodInfo.where, methodInfo.getBindArgument(args)));
                    } else {
                        return methodInfo.generateReturn(select.execute());
                    }
                case MethodInfo.SQL_TYPE_DELETE: {
                    String table = methodInfo.tableName;
                    if ("".equals(table)) {
                        return methodInfo.generateReturn(
                          SQL.with(db).delete(methodInfo.getArgument(args));
                        );
                    } else {

                    }


                }


                if (hasWhere) {
                    return methodInfo.generateReturn(method, select.where(methodInfo.where, methodInfo.getBindArgument(args)));
                } else {
                    return methodInfo.generateReturn(method, select.execute());
                }
                case MethodInfo.SQL_TYPE_UPDATE:
                    Select<?> select = SQL.with(db).select(methodInfo.tableClass);
                    if (hasWhere) {
                        return methodInfo.generateReturn(method, select.where(methodInfo.where, methodInfo.getBindArgument(args)));
                    } else {
                        return methodInfo.generateReturn(method, select.execute());
                    }
                case MethodInfo.SQL_TYPE_INSERT:
                    Insert<?> insert = SQL.with(db).insert(methodInfo.getArgument(args));
                    return methodInfo.generateReturn(method, insert.execute());
                default:
                    break;
            }

            return null;
        }

    }

    static class MethodInfo {
        public static final int SQL_TYPE_SELECT = 0;
        public static final int SQL_TYPE_DELETE = 1;
        public static final int SQL_TYPE_UPDATE = 2;
        public static final int SQL_TYPE_INSERT = 3;

        public static final int SQL_RETURN_TYPE_NUMBER = 0;
        public static final int SQL_RETURN_TYPE_OBJECT = 1;
        public static final int SQL_RETURN_TYPE_VOID = 2;

        private MethodInfo() {

        }

        int sqlType = -1;
        int sqlReturnType = 0;
        String tableName;
        Class<?> tableClass;
        String where = null;
        Class<?> returnType;
        int argIndex;

        // <sqlIndex, argsIndex >
        SparseIntArray argsBindingMap;

        public String[] getBindArgument(Object[] args) {
            String[] sqlArgs = new String[argsBindingMap.size()];
            for (int i = 0; i < sqlArgs.length; i++) {
                sqlArgs[i] = args[argsBindingMap.get(i + 1)].toString();
            }
            return sqlArgs;
        }

        public Object getArgument(Object[] args) {
            return args[argIndex];
        }

        public Object generateReturn(Object sqlResult) {
            Class<?> resType = returnType;
            if (resType == null || resType == Void.class) {
                return null;
            }

            if (resType != Observable.class) {
                return valueReturn(sqlResult);
            } else {
                final Object result = valueReturn(sqlResult);

                return Observable.create(new Observable.OnSubscribe<Object>() {
                    @Override
                    public void call(Subscriber<? super Object> subscriber) {
                        if (subscriber.isUnsubscribed()) {
                            return;
                        }
                        if (result instanceof Collection) {
                            Iterator iterator = ((Collection) result).iterator();
                            while (iterator.hasNext()) {
                                subscriber.onNext(iterator.next());
                            }
                        } else {
                            subscriber.onNext(result);
                        }
                        subscriber.onCompleted();
                    }
                });
            }
        }

        private Object valueReturn(Object sqlResult) {
            if (sqlResult == null) {
                return null;
            }
            Class<?> resType = returnType;
            if (Types.equals(resType, Integer.class)) {
                if (sqlResult instanceof Integer) {
                    return sqlResult;
                } else if (sqlResult instanceof Long) {
                    return (int) ((long) sqlResult % Integer.MAX_VALUE);
                } else if (sqlResult instanceof Collection) {
                    return ((Collection) sqlResult).size();
                }
            } else if (Types.equals(resType, Long.class)) {
                if (sqlResult instanceof Integer || sqlResult instanceof Long) {
                    return sqlResult;
                } else if (sqlResult instanceof Collection) {
                    return ((Collection) sqlResult).size();
                }
            } else if (Types.equals(resType, Collection.class)) {
                if (sqlResult instanceof Collection) {
                    return sqlResult;
                }
            } else {
                if (sqlResult instanceof Collection) {
                    return ((Collection) sqlResult).iterator().hasNext() ? ((Collection) sqlResult).iterator().next() : null;
                } else {
                    return sqlResult;
                }
            }
            return null;
        }


        public static MethodInfo process(Method method) {
            MethodInfo methodInfo = new MethodInfo();
            Annotation[] annotations = method.getAnnotations();

            for (Annotation annotation : annotations) {
                if (annotation instanceof ASelect) {
                    Class<?> clazz = ((ASelect) annotation).clazz();
                    if (clazz == Void.class) {
                        methodInfo.tableName = ((ASelect) annotation).table();
                    } else {
                        methodInfo.tableClass = clazz;
                    }
                    methodInfo.sqlType = SQL_TYPE_SELECT;
                    methodInfo.sqlReturnType = SQL_RETURN_TYPE_OBJECT;
                } else if (annotation instanceof ADelete) {
                    methodInfo.tableName = ((ADelete) annotation).table();
                    methodInfo.sqlType = SQL_TYPE_DELETE;
                    methodInfo.sqlReturnType = SQL_RETURN_TYPE_NUMBER;
                } else if (annotation instanceof AUpdate) {
                    Class<?> clazz = ((AUpdate) annotation).clazz();
                    if (clazz == Void.class) {
                        methodInfo.tableName = ((AUpdate) annotation).table();
                    } else {
                        methodInfo.tableClass = clazz;
                    }
                    methodInfo.sqlType = SQL_TYPE_UPDATE;
                    methodInfo.sqlReturnType = SQL_RETURN_TYPE_NUMBER;
                } else if (annotation instanceof AInsert) {
                    Class<?> clazz = ((AInsert) annotation).clazz();
                    if (clazz == Void.class) {
                        methodInfo.tableName = ((AInsert) annotation).table();
                    } else {
                        methodInfo.tableClass = clazz;
                    }
                    methodInfo.sqlType = SQL_TYPE_INSERT;
                    methodInfo.sqlReturnType = SQL_RETURN_TYPE_NUMBER;
                } else if (annotation instanceof AWhere) {
                    methodInfo.where = ((AWhere) annotation).value();
                }
            }

            if (methodInfo.sqlType < 0) {
                throw new IllegalArgumentException("use ASelect, ADelete, AUpdate or AInsert annotation");
            }

            methodInfo.argsBindingMap = new SparseIntArray();

            Annotation[][] parameterAnnos = method.getParameterAnnotations();
            int paramIndex = 0;
            for (Annotation[] pas : parameterAnnos) {
                for (Annotation annotation : pas) {
                    if (annotation instanceof AWhereArgs) {
                        methodInfo.argsBindingMap.put(((AWhereArgs) annotation).value(), paramIndex);
                    } else if (annotation instanceof AArgument) {
                        methodInfo.argIndex = paramIndex;
                    }
                }
                paramIndex++;
            }

            methodInfo.returnType = method.getReturnType();

            return methodInfo;
        }

    }
}
