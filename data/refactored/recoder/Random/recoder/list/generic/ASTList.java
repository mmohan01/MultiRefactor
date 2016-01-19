package recoder.list.generic;

import java.util.List;

import recoder.java.SourceElement;

protected interface ASTList<E extends  SourceElement>  extends List<E> {
    ASTList<E> deepClone();
    void trimToSize();
}
