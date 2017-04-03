/**
 * Beaver: a compiler front-end compiler
 * Copyright (c) 2003-2012, Alexander Demenchuk
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in the
 *    documentation and/or other materials provided with the distribution.
 * 3. Neither the name of the copyright holders nor the names of its
 *    contributors may be used to endorse or promote products derived from
 *    this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF
 * THE POSSIBILITY OF SUCH DAMAGE.
 */
package beaver.util;

/**
 *
 */
public class BitSet {
    private int[] bit_bags;
    private int   num_bits;
    private int   lb, ub;  // [lb, ub)

    public BitSet(int capacity) {
        bit_bags = new int[(capacity + 31) / 32];
    }

    public BitSet() {
        this(256);
    }

    /**
     * Sets a single bit to 1.
     *
     * @param i
     *            element to add to the set
     * @return true if a bit has been added and false if it was already in the set
     */
    public boolean add(int i) {
        int bag_index = getBitBagIndex(i);
        int bit_index = i & 31;
        int bit_mask = 1 << bit_index;
        boolean bit_not_set = (bit_bags[bag_index] & bit_mask) == 0;
        if (bit_not_set) {
            bit_bags[bag_index] |= bit_mask;
            num_bits += 1;
        }
        return bit_not_set;
    }

    /**
     * Adds all bits from the specified range to the set
     *
     * @param lb
     *            lower bound
     * @param ub
     *            upper bound
     */
    public void add(int lb, int ub) {
        if (num_bits == 0) {
            add(lb++);
        }
        int first_bit_bag_index = getBitBagIndex(lb);
        int last_bit = ub - 1;
        int last_bit_bag_index = getBitBagIndex(last_bit);
        if (first_bit_bag_index == last_bit_bag_index) {
            int mask = 1 << (lb & 31);
            int last_bit_mask = 1 << (last_bit & 31);
            int setmask = last_bit_mask;
            while (mask != last_bit_mask) {
                setmask |= mask;
                mask <<= 1;
            }
            int num_old_bits = BitMath.countBits(bit_bags[first_bit_bag_index]);
            bit_bags[first_bit_bag_index] |= setmask;
            int num_new_bits = BitMath.countBits(bit_bags[first_bit_bag_index]);
            num_bits += num_new_bits - num_old_bits;
        } else {
            int num_old_bits = BitMath.countBits(bit_bags[first_bit_bag_index]);
            int mask = 0x80000000 >> (31 - (lb & 31));
            bit_bags[first_bit_bag_index] |= mask;
            int num_new_bits = BitMath.countBits(bit_bags[first_bit_bag_index]);
            num_bits += num_new_bits - num_old_bits;

            for (int i = first_bit_bag_index + 1; i < last_bit_bag_index; i++) {
                num_old_bits = BitMath.countBits(bit_bags[i]);
                bit_bags[i] = -1;
                num_bits += 32 - num_old_bits;
            }

            num_old_bits = BitMath.countBits(bit_bags[last_bit_bag_index]);
            mask = ~((last_bit & 31) == 31 ? 0 : 0x80000000 >> (30 - (last_bit & 31)));
            bit_bags[last_bit_bag_index] |= mask;
            num_new_bits = BitMath.countBits(bit_bags[last_bit_bag_index]);
            num_bits += num_new_bits - num_old_bits;
        }
    }

    /**
     * Resets a single bit in the set to 0
     *
     * @param i
     *            bit number to be removed from the set
     */
    public void erase(int i) {
        if (i < lb || ub <= i)
            throw new IndexOutOfBoundsException("Bit " + i + " is out of bounds [" + lb + "," + ub + ")");

        int bag_index = (i - lb) / 32;
        int bit_index = i & 31;
        int bit_mask = 1 << bit_index;

        if ((bit_bags[bag_index] & bit_mask) != 0) {
            bit_bags[bag_index] &= ~bit_mask;
            num_bits--;
        }
    }

    /**
     * Resets bit set into initial state - no bits are set, and bounds are undefined
     */
    public void clear() {
        num_bits = 0;
        lb = ub = 0;
        for (int i = 0; i < bit_bags.length; i++) {
            bit_bags[i] = 0;
        }
    }

    /**
     * Adds every element of another set to this set.
     *
     * @param another_set
     *            set of elements to be added to this set
     * @return true if this set has new bits added
     */
    public boolean add(BitSet another_set) {
        boolean new_bits_added = false;
        if (another_set != null && another_set.num_bits > 0) {
            if (another_set.lb < this.lb || this.ub < another_set.ub) {
                int new_lb = Math.min(another_set.lb, lb);
                int new_ub = Math.max(another_set.ub, ub);
                int n_new_l_bags = (lb - new_lb) / 32;
                int n_new_u_bags = (new_ub - ub) / 32;
                int[] new_bags = new int[n_new_l_bags + bit_bags.length + n_new_u_bags];
                System.arraycopy(bit_bags, 0, new_bags, n_new_l_bags, bit_bags.length);
                bit_bags = new_bags;
                lb = new_lb;
                ub = new_ub;
            }
            int into_bag_index = getBitBagIndex(Math.max(another_set.lb, lb));
            for (int from_bag_index = 0; from_bag_index < another_set.bit_bags.length; from_bag_index++, into_bag_index++) {
                int diff = another_set.bit_bags[from_bag_index] & ~bit_bags[into_bag_index];
                if (diff != 0) {
                    bit_bags[into_bag_index] |= diff;
                    num_bits += BitMath.countBits(diff);
                    new_bits_added = true;
                }
            }
        }
        return new_bits_added;
    }

    /**
     * Checks whether the element is in the set
     *
     * @param i
     *            element to check
     * @return true if the element is present in the set
     */
    public boolean isSet(int i) {
        if (isEmpty() || i < lb || ub <= i)
            return false;
        return (bit_bags[(i - lb) >> 5] & (1 << (i & 31))) != 0;
    }

    /**
     * Checks whether the set has no set bits.
     *
     * @return true if all the bits of the set are cleared
     */
    public boolean isEmpty() {
        return num_bits == 0;
    }

    /**
     * Returns the size of the set.
     *
     * @return number of bits in the set
     */
    public int size() {
        return num_bits;
    }

    /**
     * Returns the capacity of the set, i.e. the number of bits that'll fit in without reallocation.
     *
     * @return capacity of the set (in bits)
     */
    public int capacity() {
        return bit_bags.length * 32;
    }

    /**
     * Checks whether two sets have the same bits
     */
    public boolean equals(Object o) {
        if (o instanceof BitSet) {
            BitSet other = (BitSet) o;

            if (other.num_bits == this.num_bits) {
                if (num_bits > 0) {
                    int this_idx = 0;
                    while (this.bit_bags[this_idx] == 0) {
                        this_idx++;
                    }
                    int other_idx = 0;
                    while (other.bit_bags[other_idx] == 0) {
                        other_idx++;
                    }
                    if (this.lb + this_idx * 32 != other.lb + other_idx * 32) {
                        return false;
                    }
                    while (this_idx < this.bit_bags.length && other_idx < other.bit_bags.length) {
                        if (this.bit_bags[this_idx++] != other.bit_bags[other_idx++])
                            return false;
                    }
                }
                return true;
            }
        }
        return false;
    }

    /**
     * Calculates a number that can be used as the set hash code
     */
    public int hashCode() {
        int i = 0;
        while (i < bit_bags.length && bit_bags[i] == 0)
            i++;
        int h = lb + i * 32;
        while (i < bit_bags.length) {
            if (bit_bags[i] != 0) {
                h = h * 37 + bit_bags[i];
            }
            i++;
        }
        return h;
    }

    /**
     * Invokes a bit processor for each set bit in the set.
     *
     * @param proc
     *            an action to be called
     */
    public void forEachBitAccept(BitVisitor proc) {
        if (num_bits == 0)
            return;

        for (int bag_index = 0; bag_index < bit_bags.length; bag_index++) {
            for (int bit_index = lb + bag_index << 5, bag = bit_bags[bag_index]; bag != 0; bag >>>= 1, bit_index++) {
                if ((bag & 0x0001) == 0) {
                    if ((bag & 0xFFFF) == 0) {
                        bit_index += 16;
                        bag >>>= 16;
                    }
                    if ((bag & 0x00FF) == 0) {
                        bit_index += 8;
                        bag >>>= 8;
                    }
                    if ((bag & 0x000F) == 0) {
                        bit_index += 4;
                        bag >>>= 4;
                    }
                    if ((bag & 0x0003) == 0) {
                        bit_index += 2;
                        bag >>>= 2;
                    }
                    if ((bag & 0x0001) == 0) {
                        bit_index += 1;
                        bag >>>= 1;
                    }
                }
                proc.visit(bit_index);
            }
        }
    }

    /**
     * Returns an index of a bag where new bit is going to be placed. Reallocates bags in case new
     * bit is outside current boundaries.
     *
     * @param i
     *            element to be added to the set
     * @return index of the bit bag where the bit should be set
     */
    private int getBitBagIndex(int i) {
        if (num_bits == 0) {
            lb = i & ~31;
            ub = lb + bit_bags.length * 32;

            return 0;
        } else if (i < lb) {
            int new_lb = i & ~31;
            int n_new_bags = (lb - new_lb) / 32;
            int[] new_bags = new int[bit_bags.length + n_new_bags];
            System.arraycopy(bit_bags, 0, new_bags, n_new_bags, bit_bags.length);
            bit_bags = new_bags;
            lb = new_lb;

            return 0;
        } else if (ub <= i) {
            int new_ub = (i + 32) & ~31;
            int n_new_bags = (new_ub - ub) / 32;
            int[] new_bags = new int[bit_bags.length + n_new_bags];
            System.arraycopy(bit_bags, 0, new_bags, 0, bit_bags.length);
            bit_bags = new_bags;
            ub = new_ub;

            return bit_bags.length - 1;
        } else // i E [lb,ub)
        {
            return (i - lb) / 32;
        }
    }

    /**
     * Protocol that a bit processing entity has to follow
     */
    public static interface BitVisitor {
        /**
         * This function is called for each non-zero bit in a set
         *
         * @param bit
         *            bit number
         */
        void visit(int bit);
    }
}
