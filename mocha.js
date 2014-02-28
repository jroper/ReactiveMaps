console.log("Let's try to use mocha");

var Mocha = require("mocha");

var mocha = new Mocha();

var args = process.argv;

var options = JSON.parse(args[2]);
var tests = JSON.parse(args[3]);

function MyReporter(runner) {
    var passes = 0;
    var failures = 0;

    runner.on('pass', function(test){
        passes++;
        console.log('pass: %s', test.fullTitle());
    });

    runner.on('fail', function(test, err){
        failures++;
        console.log('fail: %s -- error: %s', test.fullTitle(), err.message);
    });

    runner.on('end', function(){
        console.log('end: %d/%d', passes, passes + failures);
        process.exit(failures);
    });
}

mocha.reporter(MyReporter);

if (options.require) {
    options.require.forEach(function(r) {
       require(r);
    });
}

mocha.files = tests;
mocha.run(function(code) {
  console.log("mocha tests finished with: " + code)
  process.exit(code);
});

console.log("mocha tests started");