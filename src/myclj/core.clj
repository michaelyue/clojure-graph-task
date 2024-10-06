(ns myclj.core)

;; graph without weight edge
;;   A
;;  / \
;; B   C
;; \   /
;;   D 
(def graph {:A [:B :C],
        :B [:D],
        :C [:D],
        :D []})

;; graph with weight edge
;;     A
;;    / \
;;  B-1 C-2
;;   \   /
;;   3-D-4  
(def graphw
  {:A [[:B 1] [:C 2]]
   :B [[:D 3]]
   :C [[:D 4]]
   :D []})

;; DFS original one without weight
(defn seq-graph [d g s]
  ((fn rec-seq [explored frontier]
     (lazy-seq
      (if (empty? frontier)
        nil
        (let [v (peek frontier)
              neighbors (g v)]
          (cons v (rec-seq
                   (into explored neighbors)
                   (into (pop frontier) (remove explored neighbors))))))))
   #{s} (conj d s)))

(def seq-graph-dfs (partial seq-graph []))
(def seq-graph-bfs (partial seq-graph (clojure.lang.PersistentQueue/EMPTY)))

;; #1 solution
;; DFS with weight
(defn seq-graph-w [dest graph start]
  ((fn rec-seq [explored frontier]
     (lazy-seq
      (if (empty? frontier)
        nil
        (let [v (peek frontier)
              ;; get first element for each vector (verex and weight)
              ;; only change here to support weight
              neighbors (map first (graph v))] 
          (cons v (rec-seq
                   (into explored neighbors)
                   (into (pop frontier) (remove explored neighbors))))))))
   #{start} (conj dest start)))

(def seq-graphw-dfs (partial seq-graph-w []))
(def seq-graphw-bfs (partial seq-graph-w (clojure.lang.PersistentQueue/EMPTY)))

;; pick up one random with from 1-10
(defn pick-weight []
  (inc (rand-int 10)))

;; create vertex names from name collection randomly
(defn create-vertex-names [n]
  (mapv #(keyword (str %)) (range 1 (inc n))))

;; randomly get two vertices
(defn sample [vec]
  "shuffle and get the first 2 elements from vector"
  (let [[u v] (take 2 (shuffle vec))] 
    [u v]))


;; create a spanning tree
(defn create-spanning-tree [vertices]
  (let [edges (map vector vertices (rest vertices))]
    edges))

;; #2 solution
;; create a simple connected graph G(n,s) with N vertices and S edges
(defn create-connected-directed-graph [n s]
   ;; number of edges must be greater than n-1 
   (when (< s (dec n))
    (throw (ex-info "no enough edges to create a connected graph!" {})))
  
  (let [vertices (create-vertex-names n)
        
        ;; make sure graph is connected
        initial-tree (create-spanning-tree vertices) 

        ;; add extra random edges 
        extra-edges (repeatedly (- s (dec n)) #(sample vertices))
        ;; combine all edges with spanning tree
        all-edges (concat initial-tree extra-edges) 

        ;; add more edges if needed
        add-edge (fn [g [u v]]
                   (let [weight (pick-weight)]
                     (update g u #(if (contains? % v) % (conj % [v weight])))
                   ))
        
        ;; get a start graph
        start-graph (into {} (map #(vector % []) vertices))

        ;; try to create connected graph
        graph (reduce add-edge start-graph all-edges)

        ;; check if created graph is connected
        connected-graph (seq-graphw-dfs graph (first (keys start-graph)))]
    (if (> (count connected-graph) 1) ; for connected graph returned DFS result should be greater then one
      (do
        (println "graph connected.")
        graph)
      (do
        (println "graph not connected, trying again...")
        ;; try again, maybe sometimes there is a infinitely loop
        ;; how to avoid ? -- no time to think this time
        (recur n s)))))



;; #3 solution
;; implementation of Dijkstra's algorithm that traverses the graph list path
(defn dijkstra [g s]
  
  ;; step-1: initialize distances and predecessors
  (let [vertices (keys g)
        
        ;; set all distances to infinity
        dist (zipmap vertices (repeat Integer/MAX_VALUE))
        prev (zipmap vertices (repeat nil))
        Q (set vertices)]
    
    ;; step-2: set the distance for the source vertex to 0
    (let [dist (assoc dist s 0)]
      
      ;; step-3: process the queue
      (loop [dist dist prev prev Q Q]
        (if (empty? Q)
          ;; return distance and previous
          {:dist dist :prev prev}
          ;; get smallest one
          (let [u (apply min-key #(get dist %) Q) 
                Q (disj Q u)
                neighbors (get g u)]
            
            ;; process each neighbor v of u
            (let [result (reduce
                          (fn [[dist prev] [v weight]]
                            ;; calculate the alternative distance
                            (let [alt (+ (get dist u) weight)]
                              ;; check if the alternative distance is better
                              (if (< alt (get dist v))
                                ;; update distance and predecessor if better
                                [(assoc dist v alt) (assoc prev v u)]
                                [dist prev])))
                          [dist prev]
                          neighbors)]
              
              ;; recursion with the updated distances, predecessors, and remaining vertices
              (recur (first result) (second result) Q))))))))

;; trace the path from source to destination
(defn shortest-path [prev dest]
  (loop [current dest path []]
    (if (nil? current)
      path
      (recur (get prev current) (conj path current)))))

;; based on dijkstra algo get shortest path from one vertex to another one vertex
;; g - original graph
;; f - from vertex
;; t - to vertex
(defn get-shortest-path [g f t]
  (let [{:keys [dist prev]} (dijkstra g f)
        distance (get dist t)]
    (if (= distance Integer/MAX_VALUE)
      ;; no path found
      {:distance nil :path nil}
      ;; get shortest path
      {:distance distance :path (reverse (shortest-path prev t))})))


;; #4 solution
;; the eccentricity of a vertex v is defined as the greatest distance between v and any other vertex.
(defn eccentricity [g v]
  (let [{:keys [dist]} (dijkstra g v)] 
    ;; default max INT should be filtered, otherwise dimeter value will be wrong
    (let [getric (reduce max (filter #(not= % Integer/MAX_VALUE) (vals dist)))]
      ;;(println "graph eccentricity is:" getric)
      getric)))
    

;; the radius of a graph is the minimum eccentricity of any vertex in a graph.
(defn radius [g]
  (let [ectri (map #(eccentricity g %) (keys g))]
    ;; ingore zero value
    (let [nozero-ectri (filter #(> % 0) ectri)] 
      (let [gradius (if (seq nozero-ectri) 
        (reduce min nozero-ectri)
        0)]
        (println "graph radius is:" gradius)
        gradius
        )))) ;; but if nothing there, we still return 0 means maybe unreached node

;; the diameter of a graph is the maximum eccentricity of any vertex in a graph.
(defn diameter [g]
  (let [vertices (keys g)]
    (let [gdiameter (reduce max (map #(eccentricity g %) vertices))]
     (println "graph diameter is:" gdiameter)
     gdiameter)))

;; make it easy to call for testing
(defn random-graph []
  (create-connected-directed-graph 10 10))

;; main run
(defn -main [& args] 
  (let [gp (random-graph)]
    (println "created graph is:" gp)

    ;; Find the shortest path from :1 to :3
    (def result (get-shortest-path gp :1 :3))

    ;; Print the result
    (println "shortest distance is:" (:distance result))
    (println "path are:" (:path result))

    ;; DFS result
    (def result (doall (seq-graphw-dfs gp :3)))
    (println "DFS result is:" result)

    ;; ouput eccentricity for starting node :2
    (println "eccentricity for node :2 is:" (eccentricity gp :2))

    ;; output radius
    (radius gp)

    ;; output diameter
    (diameter gp)))

