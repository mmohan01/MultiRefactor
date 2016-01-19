// This file is part of the RECODER library and protected by the LGPL.

package recoder.util;

/**
 * Different sort algorithms based upon standard arrays and arbitrary total
 * order relations.
 * 
 * @author AL
 */
public class Sorting {

    public static void heapSort(Object[] a) {
        heapSort(a, Order.NATURAL);
    }

    public static void heapSort(Object[] a, Order ord) {
        for (int m = (a.length - 1) / 2; m >= 0; m -= 1) {
            for (int j, i = m; (j = 2 * i + 1) < a.length; i = j) {
                int k = j + 1;
                if (ord.greater(a[i], a[j])) {
                    j = i;
                }
                if (k < a.length && ord.greater(a[k], a[j])) {
                    j = k;
                }
                if (i == j) {
                    break;
                }
                Object swap = a[i];
                a[i] = a[j];
                a[j] = swap;
            }
        }
        for (int m = (a.length - 1); m >= 1; m -= 1) {
            Object swap = a[0];
            a[0] = a[m];
            a[m] = swap;
            for (int j, i = 0; (j = 2 * i + 1) < m; i = j) {
                int k = j + 1;
                if (ord.greater(a[i], a[j])) {
                    j = i;
                }
                if (k < m && ord.greater(a[k], a[j])) {
                    j = k;
                }
                if (i == j) {
                    break;
                }
                swap = a[i];
                a[i] = a[j];
                a[j] = swap;
            }
        }
    }

    public static void insertionSort(Object[] a) {
        insertionSort(a, Order.NATURAL);
    }

    public static void insertionSort(Object[] a, Order ord) {
        for (int i = 1; i < a.length; i += 1) {
            Object x = a[i];
            int j = i - 1;
            while (j >= 0 && ord.greater(a[j], x)) {
                a[j + 1] = a[j];
                j -= 1;
            }
            a[j + 1] = x;
        }
    }

    protected static void insertionSort(Object[] a, int le, int ri, Order ord) {
        for (int i = le + 1; i <= ri; i += 1) {
            Object x = a[i];
            int j = i - 1;
            while (j >= le && ord.greater(a[j], x)) {
                a[j + 1] = a[j];
                j -= 1;
            }
            a[j + 1] = x;
        }
    }

    protected final static Object median(Object x, Object y, Object z, Order o) {
        return o.less(x, y) ? (o.less(y, z) ? y : (o.less(x, z) ? z : x)) : (o.less(y, z) ? y : (o.less(z, x) ? z : x));
    }

    public static void quickSort(Object[] a) {
        quickSort(a, 0, a.length - 1, Order.NATURAL);
    }

    public static void quickSort(Object[] a, Order ord) {
        quickSort(a, 0, a.length - 1, ord);
    }

    // to do: resolve recursion
    protected static void quickSort(Object[] a, int le, int ri, Order ord) {
        if (ri > le) {
            int i = le;
            int j = ri;
            Object x = median(a[le], a[ri], a[(le + ri) / 2], ord);
            do {
                while (ord.less(a[i], x))
                    ++i;
                while (ord.less(x, a[j]))
                    --j;
                if (i <= j) {
                    Object h = a[i];
                    a[i] = a[j];
                    a[j] = h;
                    ++i;
                    --j;
                }
            } while (i <= j);
            if (j < le + 16) {
                insertionSort(a, le, j, ord);
            } else {
                quickSort(a, le, j, ord);
            }
            if (ri < i + 16) {
                insertionSort(a, i, ri, ord);
            } else {
                quickSort(a, i, ri, ord);
            }
        }
    }

    public static boolean isOrdered(Object[] a) {
        return isOrdered(a, Order.NATURAL);
    }

    public static boolean isOrdered(Object[] a, Order ord) {
        for (int i = a.length - 1; i > 0; i -= 1) {
            if (ord.greater(a[i - 1], a[i])) {
                return false;
            }
        }
        return true;
    }
}

