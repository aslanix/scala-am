(letrec ((counter 0)
         (f (lambda ()
              (letrec ((old counter)
                       (new (+ old 1)))
                (if (cas counter old new)
                  #t
                  (f)))))
         (t1 (spawn (f)))
         (t2 (spawn (f))))
  (join t1)
  (join t2)
  (= counter 2))
