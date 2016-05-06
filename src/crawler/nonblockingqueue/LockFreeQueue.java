package crawler.nonblockingqueue;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.atomic.*;

public class LockFreeQueue<T> {

	private static class QueueNode<T> {
		private volatile T item;
		private volatile QueueNode<T> next;

		QueueNode(T x, QueueNode<T> n) {
			item = x;
			next = n;
		}

		T getItem() {
			return item;
		}

		

		void setItem(T val) {
			itemUpdater.set(this, val);
		}

		QueueNode<T> getNext() {
			return next;
		}

		boolean casNext(QueueNode<T> cmp, QueueNode<T> val) {
			return nextUpdater.compareAndSet(this, cmp, val);
		}

		private static final AtomicReferenceFieldUpdater<QueueNode, QueueNode> nextUpdater = AtomicReferenceFieldUpdater
				.newUpdater(QueueNode.class, QueueNode.class, "next");
		private static final AtomicReferenceFieldUpdater<QueueNode, Object> itemUpdater = AtomicReferenceFieldUpdater
				.newUpdater(QueueNode.class, Object.class, "item");

	

		
	}
	private transient volatile QueueNode<T> head = new QueueNode<T>(null, null);

    
    private transient volatile QueueNode<T> tail = head;
    
	private static final AtomicReferenceFieldUpdater<LockFreeQueue, QueueNode> tailUpdater = AtomicReferenceFieldUpdater
			.newUpdater(LockFreeQueue.class, QueueNode.class, "tail");
	private static final AtomicReferenceFieldUpdater<LockFreeQueue, QueueNode> headUpdater = AtomicReferenceFieldUpdater
			.newUpdater(LockFreeQueue.class, QueueNode.class, "head");

	private boolean casTail(QueueNode<T> cmp, QueueNode<T> val) {
		return tailUpdater.compareAndSet(this, cmp, val);
	}

	private boolean casHead(QueueNode<T> cmp, QueueNode<T> val) {
		return headUpdater.compareAndSet(this, cmp, val);
	}
	
	
	public LockFreeQueue() {

	}

	public boolean enqueue(T e) {
		if (e == null)
			throw new NullPointerException();
		QueueNode<T> n = new QueueNode<T>(e, null);
		for (;;) {
			QueueNode<T> t = tail;
			QueueNode<T> s = t.getNext();
			if (t == tail) {
				//if the queue is empty
				if (s == null) {
					if (t.casNext(s, n)) {
						tailUpdater.compareAndSet(this, t, n);
						return true;
					}
				} else {
					tailUpdater.compareAndSet(this, t, s);
				}
			}
		}
	}

	public T dequeue() {
		for (;;) {
			QueueNode<T> h = head;
			QueueNode<T> t = tail;
			QueueNode<T> first = h.getNext();
			if (h == head) {
				if (h == t) {
					if (first == null)
						return null;
					else
						tailUpdater.compareAndSet(this, t, first);
				} else if (headUpdater.compareAndSet(this, h, first)) {
					T item = first.getItem();
					if (item != null) {
						first.setItem(null);
						return item;
					}
				}
			}
		}
	}
	
	

}