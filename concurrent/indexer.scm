;; Example taken from Dynamic Partial Order Reduction, Figure 1
(let* ((size 128)
       (max 4)
       (table (vector size 0))
       (thread (lambda (tid)
                 (let ((m 0)
                       (getmsg (lambda ()
                                 (if (< m max) (+ (* 11 (+ m 1)) tid) -1)))
                       (hash (lambda (w) (modulo (* w 7) size)))
                       (process (lambda ()
                         (if (< m max)
                           (let* ((w (+ (* 11 (+ m 1)) tid))
                                  (update (lambda (h)
                                            (if (cas-vector table h 0 w)
                                              #t
                                              (update (modulo (+ h 1) size))))))
                             (update (hash w)))
                           #f))))
                   (process))))
       (t1 (thread 1))
       (t2 (thread 2)))
  (join t1)
  (join t2))