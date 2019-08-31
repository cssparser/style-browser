# Style Browser
An example CSS browser application, using the CSS Parser.

The core piece of code loading the stylesheet is as follows: 
```
Reader r = new FileReader(pathName);
InputSource is = new InputSource(r);
CSSOMParser parser = new CSSOMParser(new SACParserCSS3());
CSSStyleSheet stylesheet = parser.parseStyleSheet(is, null, null);
```
