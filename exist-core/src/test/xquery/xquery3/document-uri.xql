xquery version "3.1";

module namespace du="http://exist-db.org/xquery/test/document-uri";

declare namespace test="http://exist-db.org/xquery/xqsuite";

declare variable $du:xml-document := document {
  <boolean>true</boolean>
};

declare variable $du:xml-document-resource := "document-uri-test.xml";
declare variable $du:xml-document-collection-uri := "/db/";
declare variable $du:xml-document-uri := concat($du:xml-document-collection-uri, $du:xml-document-resource);

declare
    %test:setUp
function du:create-persistent-document() {
  let $log-in := xmldb:login($du:xml-document-collection-uri, "admin", "")
  let $rc := xmldb:store($du:xml-document-collection-uri, $du:xml-document-resource, $du:xml-document)
  return $rc
};

declare
    %test:tearDown
function du:delete-persistent-document() {
  let $rc := xmldb:remove($du:xml-document-collection-uri, $du:xml-document-resource)
  return $rc
};

declare
    %test:assertEmpty
function du:document-uri-test-node-empty-asarg() {
    let $node := ()
    return fn:document-uri($node)
};

declare
    %test:assertEmpty
function du:document-uri-test-node-empty-context-explicit() {
    let $node := ()
    return $node/fn:document-uri(.)
};

declare
    %test:assertError('XPDY0002') (: dynamic context not present on empty-sequence :)
function du:document-uri-test-node-empty-context-implicit() {
    let $node := ()
    return $node/fn:document-uri()
};

declare
    %test:assertEmpty
function du:document-uri-test-node-nonempty-asarg() {
    let $node := <null/>
    return fn:document-uri($node)
};

declare
    %test:assertEmpty
function du:document-uri-test-node-nonempty-context-explicit() {
    let $node := <null/>
    return $node/fn:document-uri(.)
};

declare
    %test:pending('TODO: DocumentImpl cannot be cast to NodeProxy')
    %test:assertEmpty
function du:document-uri-test-node-nonempty-context-implicit() {
    let $node := <null/>
    return $node/fn:document-uri()
};

declare
    %test:assertEmpty
function du:document-uri-test-document-inmemory-asarg() {
    let $doc := fn:parse-xml('<boolean>true</boolean>')
    return fn:document-uri($doc)
};

declare
    %test:assertEmpty
function du:document-uri-test-document-inmemory-context-explicit() {
    let $doc := fn:parse-xml('<boolean>true</boolean>')
    return $doc/fn:document-uri(.)
};

declare
    %test:pending('TODO: DocumentImpl cannot be cast to NodeProxy')
    %test:assertEmpty
function du:document-uri-test-document-inmemory-context-implicit() {
    let $doc := fn:parse-xml('<boolean>true</boolean>')
    return $doc/fn:document-uri()
};

declare
    %test:assertEquals('pass')
function du:document-uri-test-document-persistent-asarg() {
    let $doc := doc($du:xml-document-uri)
    let $uri := fn:document-uri($doc)
    return if ($uri eq $du:xml-document-uri) then ('pass') else ('error')
};

declare
    %test:assertEquals('pass')
function du:document-uri-test-document-persistent-context-explicit() {
    let $doc := doc($du:xml-document-uri)
    let $uri := $doc/fn:document-uri(.)
    return if ($uri eq $du:xml-document-uri) then ('pass') else ('error')
};

declare
    %test:assertEquals('pass')
function du:document-uri-test-document-persistent-context-implicit() {
    let $doc := doc($du:xml-document-uri)
    let $uri := $doc/fn:document-uri()
    return if ($uri eq $du:xml-document-uri) then ('pass') else ('error')
};

