# Clojure for fun

my clojure coding testing
mainly related to the graph algorithm

# How to run & test
## Run by lein run
run directly with lein run command  
go to src/myclj
type command : lein run  

result as below:  

graph connected.  
**created graph** is: {:10 [[:6 1]], :4 [[:5 9]], :7 [[:8 8]], :1 [[:2 10]], :8 [[:9 2]], :9 [[:10 4]], :2 [[:3 2]], :5 [[:6 3]], :3 [[:4 9]], :6 [[:7 3]]}  
**shortest distance** is: 12  
**path** are: (:1 :2 :3)  
**DFS result** is: (:3 :4 :5 :6 :7 :8 :9 :10)  
**eccentricity** for node :2 is: 40  
**graph radius is**: 10  
**graph diameter** is: 50  

  
actually default lein run will call the following functions:  
    

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

## Run in REPL
- **create a directed connected graph**  
  (random-graph)  
  or  
  (create-connected-directed-graph N S)  
  for example  
  (create-connected-directed-graph 10 10)
  
- **get shortest distance**  
  (get-shortest-path (random-graph) :1 :3)  
  will return result as a map like this:
  {:distance 12, :path (:1 :2 :3)}
  
- **get eccentricity**  
  (eccentricity (random-graph) :1)
  
- **get radius**  
  (radius (random-graph))
  
- **get diameter**     
  (diameter (random-graph)) 
