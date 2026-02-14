(ns mnd-jmoons.main        ;;  -*- mode: Clojurescript-*-
  (:require  [mnd-jmoons.vsp  :as vp]  [mnd-jmoons.jupmoons :as jm]
             [mnd-jmoons.mthu :as m]   [mnd-jmoons.ut2tdb :as ut]
             [clojure.string :as str]
             (goog.string.format) )    )


;; layer over the math models for Earth, Jupiter, Galilean moons
(defn get1t1Ld2pos "values for one step in simulation" [timeJDU]
  (let [t0           (ut/UT_to_TDB timeJDU)
        lightD0      (vp/calcLightDelay 4 t0)        ;; 4 :: Jupiter
        jpos0        (vp/getEclipticPosition     4 (- t0 lightD0))
        jSunPos0     (vp/getHeliocentricPosition 4 (- t0 lightD0))  ]
    {:t0 t0   :lightDelay0 lightD0   :jpos0 jpos0   :jSunPos0 jSunPos0 } )  )


(defn shapeS [mapXt0]
  (let [{:keys [t0  lightDelay0  jpos0  jSunPos0 ]}    mapXt0
        pos0     (jm/fourMoonsValuesV jpos0    t0 lightDelay0)
        sunPos0  (jm/fourMoonsValuesV jSunPos0 t0 lightDelay0)    ]

    ;;reshape  above for desired access pattern of simulation
    [ [ (pos0 0)  (sunPos0 0)  ]       ;; Io  2pos @ 1time
      [ (pos0 1)  (sunPos0 1)  ]       ;; Europa
      [ (pos0 2)  (sunPos0 2)  ]       ;; Ganymede
      [ (pos0 3)  (sunPos0 3)  ] ])  ) ;; Callisto


(def Dheight 150)          (def Dwidth 800) ;; arbitrary?
(def yctr  (/ Dheight 2))  (def xctr  (/ Dwidth 2))

(def WINDOW_HEIGHT (+ Dheight 20))  ;; quick kludge!  TODO
;;(def WINDOW_HEIGHT (+ Dheight 10 (((keyinfo 0)0)3) 10 ))
;;  (((keyinfo 0)0)3) :: 24   thus WINDOW_HEIGHT 24+20 ?? TODO

;;(def discWidth  (/ Dwidth  28.0))  ;; Jupiter baseRadiusWidth
(def radius     (/ (/ Dwidth  28.0) 2.0) )  ;; was discWidth / 2


(defn draw-shadow "" [ctx  vpos]  ;; s/sun.java2d.SunGraphics2 g/ctx/ ?
  (let [[pos sunpos] vpos  ;; c/b [ _ sunpos] vpos  ??
        ptx  (+ xctr (m/iround (* (:x sunpos) radius) ))
        pty  (- yctr (m/iround (* (:y sunpos) radius) )) ]
    (when (:inFront sunpos)
      (set! (. ctx -fillStyle) "black")    ;; shadow as 3x3 rect
      (.fillRect ctx (dec ptx) (dec pty) 3 3)  )   ) )


(defn draw-moon-and-label "" [ctx  vpos]
  (let [[pos sunpos] vpos
        ^long ptx     (+ xctr (m/iround (* (:x pos) radius) ))
        ^long pty     (- yctr (m/iround (* (:y pos) radius) ))
        ml-color      (cond  (:behind  sunpos)  "blue"     ;; eclipsed
                             (:inFront pos)     "gray"     ;; transit
                             :else              "white" )
        label-string  (subs (:name pos) 0 2)  ]  ;; two-letter label
     (when (not (:behind pos))     ;; i.e. not occluded
       (set! (. ctx -fillStyle) ml-color)
       (.fillRect ctx (dec ptx) (dec pty) 3 3) ;;  moon as 3x3 rect
       (set! (. ctx -font) "12px sans-serif")  ;;    <- was monospace
       (.fillText ctx label-string  (+ ptx 6) (+ pty 8) )  )   )   )


(def sim-tse (atom {:t 0  :step (/ 10.0 1440)  :ena 1}))
(def tdltv [-1.0 (/ -60.0 1440) (/ -5.0 1440) (/ 5.0 1440) (/ 60.0 1440) 1.0])


(defn init-state "t  as timeJDU at today's  m d y  at midnight UTC" []
  (let [n  (js/Date.)  ]
    (swap! sim-tse assoc :t             ;;  .-- make month 1-origin !
           (m/get-jd (. n getUTCFullYear)  (inc (. n getUTCMonth))
                     (. n getUTCDate)  0 0 0)  )
    )  )


(defn draw-msg "" [ctx px py fstr]  ;; was  monospace  -------.
  (set! (. ctx -fillStyle) "red")   (set! (. ctx -font) "16px sans-serif")
  (.fillText ctx fstr px  py )  )


(defn paintComponent ""  []
  ;;  update state -- as a timer tick has occurred
  (let [{:keys [t step] }  @sim-tse ]
    (swap! sim-tse assoc :t (+ t step ))  )
  
  (let [t               (:t @sim-tse)
        vvpos           (->> (get1t1Ld2pos t)   (shapeS  ) )
        [y mn d h m s]  (m/getDate t)
        ctx     (.getContext (.getElementById js/document "myCanvas") "2d")  ]

    (set! (. ctx -fillStyle) "black")
    (.fillRect ctx 0 0  Dwidth WINDOW_HEIGHT)
    ;;(draw-btns g)  OMIT for now.  Was only use of fn  draw-string !

    (draw-msg  ;;  in lieu of  .drawString   Or inline?  TODO
     ctx  (int(+ xctr 170))  (int(+ yctr 65))
     (goog.string.format  "UTC %4d-%02d-%02d  %2d:%02d"  y mn d h m) )


    ;; simplify as a circle?
    (.beginPath ctx)
    (set! (. ctx -fillStyle) "#FFCC66")  (set! (. ctx -strokeStyle) "orange")
    (.arc ctx  xctr yctr radius  0  vp/TwoPI )  ;;  reqs. 5 args
    (.stroke ctx)
    (.fill ctx)
    

    (doseq [vv  vvpos] ;; i.e. each element in  vvpos
      (draw-shadow         ctx vv )
      (draw-moon-and-label ctx vv ) )
    )  )

;;(println "Hello-jmoons!") ;; ala cljs-hello
(defn parse-params  "Parse URL parameters into a hashmap"  []
  (let [param-strs
        (-> (.-location js/window)
            (str/split #"\?") last (str/split #"\&")) ]
    (into {} (for [[k v] (map #(str/split % #"=") param-strs)]
               [(keyword k) v]))) )
;;(println (parse-params ) )
(println "loc..."  (.-location js/window) )

(init-state )
(paintComponent)
(js/setInterval  paintComponent 100)  ;; ntrval : 2000 in sunmap




;;  s://developer.mozilla.org/en-US/docs/Web/API/CanvasRenderingContext2D/arc


;;(def label-font (Font. "SansSerif" Font/PLAIN 10) )
;;(def time-font  (Font. "SansSerif" Font/BOLD  12) )

;;(def discHeight (/ discWidth 1.069)) ;; Jupiter flattening
;;(def iRhalfDiscHeight (m/iround (/ discHeight 2.0)) )
;;(def iRdiscWidth      (m/iround discWidth))
;;(def iRdiscHeight     (m/iround discHeight))
