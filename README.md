# Minimal Dependency Web Galilean Moons of Jupiter

A simplified build and deploy of Galilean Moons visualization, ala Clojure app [jmoons-events](https://github.com/lrngquest/jmoons-events).

- build  (with Clojure version 1.12.0.1517 including cljs version 1.12.42 )
```
 % clojure -M --main cljs.main --optimizations advanced -c mnd-jmoons.main
```

  - local test with included server and browser
  
```
 % clojure -M -m cljs.main --serve
Serving HTTP on localhost port 9000
```
(opened   http://localhost:9000  in Safari and it worked;
 then C-C to end server. )


- deploy: push to GitHub repo where  GitHub Pages  is enabled

```
...
$ git add   index.html  main.js
$ git commit -m "..."
$ git push -u origin main
```


## Credits
Clojurescript sources are lightly modified Clojure files from project [jmoons-events](https://github.com/lrngquest/jmoons-events) which in turn were translated or freely paraphrased from Java files in SkyviewCafe which carry the following notice:

    ```
    Copyright (C) 2000-2007 by Kerry Shetline, kerry@shetline.com.

    This code is free for public use in any non-commercial application. All
    other uses are restricted without prior consent of the author, Kerry
    Shetline. The author assumes no liability for the suitability of this
    code in any application.

    2007 MAR 31   Initial release as Sky View Cafe 4.0.36.
    ```

## Usage
Open the page  https://lrngquest.github.io/min-dep-webjmoons/ and a visualization of the four Galilean moons will appear, initialized at OO:OO UTC on the date when the page was opened. Simulated time is updated as the moon positions and shadows change, continuing until the page is closed.

### Limitations
Unlike jmoons-events there are no controls yet for run,stop  or stepping simulated time.

### Bugs
None known.

## License

Other than credited above:
Copyright © 2018-2026   L. E. Vandergriff

Distributed under the Eclipse Public License either version 1.0 or (at your option) any later version.

   ```
"Freely you have received, freely give."  Mt. 10:8
   ```
