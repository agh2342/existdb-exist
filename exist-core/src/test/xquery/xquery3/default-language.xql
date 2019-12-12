xquery version "3.1";

module namespace dl="http://exist-db.org/xquery/test/default-language";

declare namespace test="http://exist-db.org/xquery/xqsuite";

declare
    %test:assertError('FOER0000')
function dl:default-language-test-01() {
    let $node := ()
    return fn:default-language()
};

declare
    %test:assertError('FOER0000')
function dl:default-language-test-02() {
    let $node := <null/>
    return $node/fn:default-language()
};

