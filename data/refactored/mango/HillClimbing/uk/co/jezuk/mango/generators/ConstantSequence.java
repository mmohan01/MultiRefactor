package uk.co.jezuk.mango.generators;

import uk.co.jezuk.mango.Generator;

/**
 * Returns seed, seed, ... on successive calls to fn()
 * @author Jez Higgins, jez@jezuk.co.uk
 */
public abstract class ConstantSequence<R>  implements Generator<R> {
    private R seed_;

    public ConstantSequence( R seed)
     {
        seed_ = seed;
    } // ConstantSequence

    public
     abstract R fn() { return seed_; }
} // class ConstantSequence
