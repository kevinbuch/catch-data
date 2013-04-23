(ns com.gfredericks.catch-data-test
  (:require [clojure.test :refer :all]
            [com.gfredericks.catch-data :refer [try+]]))

(deftest normal-test
  (is (= 13 (try+ (throw (ex-info "aarg" {:foo 12}))
                  (catch-data :foo {x :foo} (inc x))))))

(deftest bad-form-test
  (is (thrown? AssertionError
               (eval
                `(try+
                  (+ 1 34)
                  (catch Exception e
                    48)
                  (fournally "nine"))))))

(deftest ex-binding-test
  (is (instance? clojure.lang.ExceptionInfo
                 (try+
                  (str "some" "dumb" :expression)
                  (throw (ex-info "Charlie" {1 2 3 4}))
                  (catch-data (constantly true)
                              {:ex my-ex}
                              my-ex)))))

(deftest symbol-binder-test
  (is (= 42 (try+
             (throw (ex-info "" {5 6 7 8}))
             (catch-data #(contains? % 7)
                         m
                         (-> m
                             (get 5)
                             (+ 36)))))))

(deftest finally-test
  (testing "without error"
    (let [a (atom 0)]
      (is (= 42 (try+
                 (* 2 3 7)
                 (catch AssertionError t
                   "this code can't be reached")
                 (catch-data :foo bar bar)
                 (finally (swap! a inc)))))
      (is (= 1 @a))))
  (testing "with error"
    (let [a (atom 0)]
      (is (= "Oh noes!"
             (try+
              (assert false)
              (catch AssertionError t
                "Oh noes!")
              (catch-data :foo bar bar)
              (finally (swap! a inc)))))
      (is (= 1 @a)))))
