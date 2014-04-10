function XMLtoJSON(xml, ignored) {
  var r, children = xml.children(), attributes = xml.@*, length = children.length();
  if(length == 0) {
    r = xml.toString();
  } else if(length == 1) {
    var text = xml.text().toString();
    if(text) {
      r = text;
    }
  }
  if(r == undefined) { 
    r = {};
    for each (var child in children) {
     var name = child.localName();
     var json = XMLtoJSON(child, ignored);
     var value = r[name];
     if(value) {
       if(value.length) {
         value.push(json);
       } else {
         r[name] = [value, json]
       }
     } else {
       r[name] = json;
     }
    }
  }
  if(attributes.length()) {
    var a = {}, c = 0;
    for each (var attribute in attributes) {
      var name = attribute.localName();
      if(ignored && ignored.indexOf(name) == -1) {
        a["_" + name] = attribute.toString();
        c ++;
      }
    }
    if(c) {
      if(r) a._ = r;
      return a;
    }
  }
  return r;
}

