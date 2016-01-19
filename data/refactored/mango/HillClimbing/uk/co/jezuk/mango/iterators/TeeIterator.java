package uk.co.jezuk.mango.iterators;

import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.List;
import java.util.ArrayList;

public abstract class TeeIterator<T>  {

    abstract public <T> List<Iterator<T>> wrap( Iterator<T> iterator, int count)
     { Buffer<T> buffer = new Buffer<T>(iterator, count); List<Iterator<T>> iterators = new ArrayList<Iterator<T>>();
        for (int i = 0; i != count; ++i)
         iterators.add(new WrapIterator<T>(buffer));
        return iterators;
    } // wrap

    static
     abstract private class Buffer<T>  {
        static abstract private class Item<T>  {
            public Item( int i, T t)
             {
                index = i;
                useCount = 0;
                item = t;
            } // Item

            public int index;
            public int useCount;
            public T item;
        } // class Item

        private int index_ = 0;
        private int useCount_;
        private Iterator<T> iterator_;
        private List<Item<T>> buffer_;

        public Buffer( Iterator<T> iterator, int count)
         {
            useCount_ = count;
            iterator_ = iterator;
            buffer_ = new ArrayList<Item<T>>();
        } // Buffer

        public
         abstract T get( int index)
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

        public
         abstract boolean has( int index)
         {
            if (index < index_)
             return true;

            while ((iterator_.hasNext()) && (index >= index_))
             buffer_.add(new Item<T>(index_++, iterator_.next()));

            return (index < index_);
        } // has
    } // class Buffer

    static
     abstract private class WrapIterator<T>  implements Iterator<T> {
        private Buffer<T> buffer_;
        private int index_;

        public WrapIterator( Buffer<T> buffer)
         {
            buffer_ = buffer;
            index_ = 0;
        } // WrapIterator

        public
         abstract T next()
         {
            return buffer_.get(index_++);
        } // next

        public
         abstract boolean hasNext()
         {
            return buffer_.has(index_);
        } // hasNext

        public
         abstract void remove()
         {
            throw new UnsupportedOperationException("uk.co.jezuk.mango.TeeIterator doesn't do remove.  Sorry, pal");
        } // remove
    } // class WrapIterator

    private TeeIterator() {}
} // class TeeIterator
