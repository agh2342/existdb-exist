xquery version "3.1";

module namespace fi="http://exist-db.org/xquery/test/format-integer";

declare namespace test="http://exist-db.org/xquery/xqsuite";

(:
fn:format-integer($value as xs:integer?, $picture as xs:string) as xs:string
fn:format-integer($value as xs:integer?, $picture as xs:string, $lang as xs:string?) as xs:string
:)
 
declare
    %test:assertEquals('""')
function fi:format-integer-test-01() {
    let $node := ()
    return fn:format-integer($node, '')
};

declare
    %test:assertEquals('"0123"')
function fi:format-integer-test-02() {
    let $node := ()
    return fn:format-integer(123, '0000')
};

declare
    %test:assertEquals('"one hundred and twenty-three"')
function fi:format-integer-test-03() {
    let $node := ()
    return fn:format-integer(123, 'w', 'en')
};

declare
    %test:assertEquals('"21st"')
function fi:format-integer-test-04() {
    let $node := ()
    return fn:format-integer(21, '1;o', 'en')
};

declare
    %test:assertEquals('"Vierzehnte"')
function fi:format-integer-test-05() {
    let $node := ()
    return fn:format-integer(14, 'Ww;o(-e)', 'de')
};

declare
    %test:assertEquals('"g"')
function fi:format-integer-test-06() {
    let $node := ()
    return fn:format-integer(7, 'a')
};

declare
    %test:assertEquals('"LVII"')
function fi:format-integer-test-07() {
    let $node := ()
    return fn:format-integer(57, 'I')
};

declare
    %test:assertEquals('"1;234"')
function fi:format-integer-test-08() {
    let $node := ()
    return fn:format-integer(1234, '#;##0;')
};

