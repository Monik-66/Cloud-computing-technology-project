const assert = require('assert');

function add(a, b) {
    return a + b;
}

// Intentionally failing test
assert.strictEqual(add(2, 3), 10);