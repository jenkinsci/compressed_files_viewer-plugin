# Compressed Files Viewer

## Introduction

This Jenkins plugin allows you to browse compressed files in the artifacts list. The decompression process is performed on the client-side for less labor on the server.

## How to use
<table style="width:100%">
  <tr>
    <th>Select artifacts</th>
    <th>Tap the plugin button</th>
  </tr>
  <tr>
    <td><img src="pics/1.png"/></td>
    <td><img src="pics/2.png"/></td>
  </tr>
  <tr>
    <td colspan="2">Now, if you are running jenkins instance in a server, you will wait a little bit(depending on the size of the artifacts) until the compressed files are extracted. 
Otherwise, if the jenkins instance is running in localhost, the artifacts will be downloaded and you won't be able to use the plugin since Javascript can't have access to local files.</td>
  </tr>
</table>

## Contributing

Refer to [contribution guidelines](https://github.com/jenkinsci/.github/blob/master/CONTRIBUTING.md)

## License
```
MIT License

Copyright (c) 2021 Ahmed Sellami

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all
copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
SOFTWARE.
```
