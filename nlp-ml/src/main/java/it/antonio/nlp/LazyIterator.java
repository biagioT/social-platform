package it.antonio.nlp;

import java.util.Iterator;
import java.util.NoSuchElementException;

public abstract class LazyIterator<T> implements Iterator<T> {

	T next = null;

	enum State {
		READY, NOT_READY, DONE;
	}

	State state = State.NOT_READY;

	@Override
	public void remove() {
		throw new UnsupportedOperationException();
	}

	@Override
	public final boolean hasNext() {
		switch (state) {
			case DONE:
				return false;
			case READY:
				return true;
			case NOT_READY:
				return tryToComputeNext();
			default:
				throw new IllegalStateException();
		}
		
	}

	boolean tryToComputeNext() {
		next = computeNext();
		if (state != State.DONE) {
			state = State.READY;
			return true;
		}
		return false;
	}

	@Override
	public final T next() {
		if (!hasNext()) {
			throw new NoSuchElementException();
		}
		state = State.NOT_READY;
		return next;
	}

	/**
	 *
	 * @return a dummy value which if returned by the <code>computeNext()</code>
	 *         method, signals that there are no more elements to return
	 */
	protected final T endOfData() {
		state = State.DONE;
		return null;
	}

	/**
	 * @return The next element which the iterator should return, or the result of
	 *         calling <code>endOfData()</code> if there are no more elements to
	 *         return
	 */
	protected abstract T computeNext();
	
}