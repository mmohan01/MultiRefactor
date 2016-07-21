package uk.co.jezuk.mango.iterators;

import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.List;
import java.util.ArrayList;

public class TeeIterator<T>  {
    static public <T> List<Iterator<T>> wrap(final Iterator<T> iterator,
                                final int count)
     {
        final Buffer<T> buffer = new Buffer<T>(iterator, count);
        final List<Iterator<T>> iterators = new ArrayList<Iterator<T>>();
        for (final int i = 0; i != count; ++i)
         iterators.add(new WrapIterator<T>(buffer));
        return iterators;
    } // wrap

    static private class Buffer<T>  {
        static private class Item<T>  {
            public Item(final int i, final T t)
             {
                index = i;
                useCount = 0;
                item = t;
            } int index; // Item
            public int useCount;
            public T item;
        } // class Item

        private int index_ = 0;
        private final int useCount_;
        private final Iterator<T> iterator_;
        private final List<Item<T>> buffer_;

        public Buffer(final Iterator<T> iterator, final int count)
         {
            useCount_ = count;
            iterator_ = iterator;
            buffer_ = new ArrayList<Item<T>>();
        } // Buffer

        public T get(final int index)
         {
            if (!has(index))
             throw new NoSuchElementException();

            T result = null;

            for (Item<T> i: buffer_)
             {
                if (i.index != index)
                 continue;

                result = i.item;
                ++i.useCount;
                if (i.useCount == useCount_)
                 buffer_.remove(i);
                break;
            } // for ...

            return result;
        } // get

        public boolean has(final int index)
         {
            if (index < index_)
             return true;

            while ((iterator_.hasNext()) && (index >= index_))
             buffer_.add(new Item<T>(index_++, iterator_.next()));

            return (index < index_);
        } // has
    } // class Buffer

    static private class WrapIterator<T>  implements Iterator<T> {
        private final Buffer<T> buffer_;
        private int index_;

        public WrapIterator(final Buffer<T> buffer)
         {
            buffer_ = buffer;
            index_ = 0;
        } // WrapIterator

        public T next()
         {
            return buffer_.get(index_++);
        } // next

        public boolean hasNext()
         {
            return buffer_.has(index_);
        } // hasNext

        public void remove()
         {
            throw new UnsupportedOperationException("uk.co.jezuk.mango.TeeIterator doesn't do remove.  Sorry, pal");
        } // remove
    } // class WrapIterator

    private TeeIterator() {}
} // class TeeIterator
