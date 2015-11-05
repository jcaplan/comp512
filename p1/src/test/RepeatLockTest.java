package test;

import lockmanager.*;

class RepeatLockTest {
    public static void main (String[] args) {
        MyThread1 t1, t2;
	LockManager lm = new LockManager ();
	t1 = new MyThread1 (lm, 1);
	t2 = new MyThread1 (lm, 2);
	t1.start ();
	t2.start ();
    }
}

class MyThread1 extends Thread {
    LockManager lm;
    int threadId;

    public MyThread1 (LockManager lm, int threadId) {
        this.lm = lm;
	this.threadId = threadId;
    }

    public void run () {
        if (threadId == 1) {
	    try {
		System.out.println(lm.Lock (1, "a", LockManager.READ));
	    }
	    catch (DeadlockException e) {
	        System.out.println ("Deadlock.... ");
	    }



	    try {
		System.out.println(lm.Lock (1, "a", LockManager.WRITE));
	    }
	    catch (DeadlockException e) {
	        System.out.println ("Deadlock.... ");
	    }
	    
	    try {
	        this.sleep (4000);
	    }
	    catch (InterruptedException e) { }
	    
	    
	    lm.UnlockAll (1);
	}
	else if (threadId == 2) {
	    try {
	        this.sleep (1000);
	    }
	    catch (InterruptedException e) { }

	    try {
		lm.Lock (2, "a", LockManager.READ);
	    }
	    catch (DeadlockException e) { 
	        System.out.println ("Deadlock.... ");
	    }

	    try {
	        this.sleep (1000);
	    }
	    catch (InterruptedException e) { }

	    try {
		lm.Lock (2, "a", LockManager.WRITE);
	    }
	    catch (DeadlockException e) { 
	        System.out.println ("Deadlock.... ");
	    }
	    
	    lm.UnlockAll (2);
		}
	}
}
