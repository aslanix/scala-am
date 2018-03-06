;; From SOTER benchmarks (pipe). Adapted to use a fixed number of actors that can be distinguished by the analyzer.
;; Bound: pipe-node bounded by 1
(letrec ((pipe-node (a/actor "pipe-node" (f next)
                           (message (m)
                                    (a/send next message (f m))
                                    (a/become pipe-node f next))))
         (sink-actor (a/actor "sink" ()
                            (message (m) (a/terminate))))
         (sink (a/create sink-actor))
         (N 3)
         (f (lambda (x) (+ x 1)))
         (p1 (a/create pipe-node f sink))
         (p2 (a/create pipe-node f p1))
         (head (a/create pipe-node f p2)))
  (a/send head message 0))
