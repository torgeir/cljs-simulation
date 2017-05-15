# cljs-simulation

[![Demo video](https://cl.ly/2T311r0b2h2o/Image%202017-05-15%20at%2011.39.12%20PM.public.png)](https://cl.ly/3c3D3G1e3v2j)

## prerequisits

Install [`boot`](https://github.com/boot-clj/boot#install)!

## run/watch

```
boot dev
```

## hot reload cljs and css

- visit `http://localhost:8080`
- change `core.cljs` to watch the js reload!
- change `app.main.less` to watch the css reload!

## connect the cljs browser repl

- connect to the repl server (e.g. with `cider-connect` in emacs, or [like this
in cursive](https://cursive-ide.com/userguide/repl.html#remote-repls). The port
number is logged when you run `boot dev`.)
- at the prompt `boot.user>` type `(boot-figwheel/cljs-repl)`
- type `(js/alert "woah!")` to watch an alert box pop open in the browser
- woah!

## useful boot tasks

Build a single composed task, e.g. to build a jar file

```sh
boot dist
```

Show help for a task, e.g. the `target` task

```sh
boot target -h
```

Run a single task and pipe the resulting file set to `target/`

```sh
boot less -- target --no-clean
```

Build, serve the `target/public` folder, and keep the server up with the `wait` task

```sh
boot build -- serve -d target/public -- wait
```
