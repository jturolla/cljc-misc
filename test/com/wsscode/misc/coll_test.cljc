(ns com.wsscode.misc.coll-test
  (:require
    [clojure.test :refer [deftest is are run-tests testing]]
    [com.wsscode.misc.coll :as coll]))

(deftest distinct-by-test
  (is (= (coll/distinct-by :id
                           [{:id   1
                             :name "foo"}
                            {:id   2
                             :name "bar"}
                            {:id   1
                             :name "other"}])
         [{:id   1
           :name "foo"}
          {:id   2
           :name "bar"}])))

(deftest dedupe-by-test
  (is (= (coll/dedupe-by :id
                         [{:id   1
                           :name "foo"}
                          {:id   1
                           :name "dedup-me"}
                          {:id   2
                           :name "bar"}
                          {:id   1
                           :name "other"}])
         [{:id   1
           :name "foo"}
          {:id   2
           :name "bar"}
          {:id   1
           :name "other"}])))

(deftest index-by-test
  (is (= (coll/index-by :id
                        [{:id   1
                          :name "foo"}
                         {:id   1
                          :name "dedup-me"}
                         {:id   2
                          :name "bar"}
                         {:id   1
                          :name "other"}])
         {1 {:id 1, :name "other"}, 2 {:id 2, :name "bar"}})))

(deftest sconj-test
  (is (= (coll/sconj nil 42) #{42}))
  (is (set? (coll/sconj nil 42))))

(deftest vconj-test
  (is (= (coll/vconj nil 42) [42]))
  (is (vector? (coll/vconj nil 42))))

(deftest queue-test
  (let [queue (-> (coll/queue)
                  (conj 1 2))]
    (is (= queue [1 2]))
    (is (= (peek queue) 1))
    (is (= (pop queue) [2])))

  (let [queue (coll/queue [1 2])]
    (is (= queue [1 2]))
    (is (= (peek queue) 1))
    (is (= (pop queue) [2]))))

(deftest map-keys-test
  (is (= (coll/map-keys inc {1 :a 2 :b})
         {2 :a 3 :b})))

(deftest filter-keys-test
  (is (= (coll/filter-keys simple-keyword? {1 :a 2 :b "foo" 3 :bar 4})
         {:bar 4})))

(deftest filter-vals-test
  (is (= (coll/filter-vals simple-keyword? {1 :a 2 :b "foo" 3 :bar 4})
         {1 :a 2 :b})))

(deftest remove-keys-test
  (is (= (coll/remove-keys number? {1 :a 2 :b "foo" 3 :bar 4})
         {"foo" 3 :bar 4})))

(deftest remove-vals-test
  (is (= (coll/remove-vals number? {1 :a 2 :b "foo" 3 :bar 4})
         {1 :a 2 :b})))

(deftest map-vals-test
  (is (= (coll/map-vals inc {:a 1 :b 2})
         {:a 2 :b 3})))

(deftest keys-set-test
  (is (= (coll/keys-set {:a 1 :b 2}) #{:a :b}))
  (is (= (coll/keys-set 5) nil)))

(deftest merge-grow-test
  (is (= (coll/merge-grow) {}))
  (is (= (coll/merge-grow {:foo "bar"}) {:foo "bar"}))

  (testing "merge sets by union"
    (is (= (coll/merge-grow {:foo #{:a}} {:foo #{:b}})
           {:foo #{:a :b}})))

  (testing "merge maps"
    (is (= (coll/merge-grow {:foo {:a 1}} {:foo {:b 2}})
           {:foo {:a 1 :b 2}})))

  (testing "keep left value if right one is nil"
    (is (= (coll/merge-grow {:foo {:a 1}} {:foo {:a nil}})
           {:foo {:a 1}}))))

(deftest merge-defaults-test
  (is (= (coll/merge-defaults {:a 1} {:b 2})
         {:a 1 :b 2}))
  (is (= (coll/merge-defaults {:a 1} {:a 2})
         {:a 1})))

(deftest update-contained-test
  (is (= (coll/update-contained {:foo 3} :foo inc)
         {:foo 4}))
  (is (= (coll/update-contained {:foo nil} :foo #(str % " bla"))
         {:foo " bla"}))
  (is (= (coll/update-contained {} :foo inc)
         {})))

(deftest update-if-test
  (is (= (coll/update-if {:foo 3} :foo inc)
         {:foo 4}))
  (is (= (coll/update-if {:foo nil} :foo inc)
         {:foo nil}))
  (is (= (coll/update-if {} :foo inc)
         {})))

(defrecord CustomRecord [])

(deftest native-map?-test
  (is (= true (coll/native-map? {})))
  (is (= true (coll/native-map? {:foo "bar"})))
  (is (= true (coll/native-map? (zipmap (range 50) (range 50)))))
  (is (= false (coll/native-map? (->CustomRecord)))))

(deftest restore-order-test
  (is (= (coll/restore-order
           [{:my.entity/id 1} {:my.entity/id 2}]
           :my.entity/id
           [{:my.entity/id    2
             :my.entity/color :my.entity.color/green}
            {:my.entity/id    1
             :my.entity/color :my.entity.color/purple}])
         [{:my.entity/id    1
           :my.entity/color :my.entity.color/purple}
          {:my.entity/id    2
           :my.entity/color :my.entity.color/green}]))
  (is (= (coll/restore-order
           [{:my.entity/id 1}
            {:my.entity/id 2}
            {:my.entity/id 3}]
           :my.entity/id
           [{:my.entity/id    3
             :my.entity/color :my.entity.color/green}
            {:my.entity/id    1
             :my.entity/color :my.entity.color/purple}])
         [{:my.entity/id    1
           :my.entity/color :my.entity.color/purple}
          {:my.entity/id 2}
          {:my.entity/id    3
           :my.entity/color :my.entity.color/green}]))
  (is (= (coll/restore-order [{:my.entity/id 1}
                              {:my.entity/id 2}
                              {:my.entity/id 3}]
                             :my.entity/id
                             [{:my.entity/id    3
                               :my.entity/color :my.entity.color/green}
                              {:my.entity/id    1
                               :my.entity/color :my.entity.color/purple}]
                             (fn [x] (assoc x :my.entity/color nil)))
         [{:my.entity/id    1
           :my.entity/color :my.entity.color/purple}
          {:my.entity/id    2
           :my.entity/color nil}
          {:my.entity/id    3
           :my.entity/color :my.entity.color/green}])))

(deftest conj-at-index-test
  (is (= (coll/conj-at-index [:a :b] 0 :c)
         [:c :a :b]))
  (is (= (coll/conj-at-index [:a :b] 1 :c)
         [:a :c :b]))
  (is (= (coll/conj-at-index [:a :b] 2 :c)
         [:a :b :c])))
