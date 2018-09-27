var dependencyGraphs = new Array()
window.onload = function() {
   for (i=0; i<dependencyGraphs.length; i++) {
      dependencyGraphs[i]();
   }
   dependencyGraphs = null;
}
function addDependencyGraph(index, fun) {
    dependencyGraphs.push(function() {
        Reveal.addEventListener( 'show-dep-graph' + index, function( event ) {
	          var cy = cytoscape({
               container: document.getElementById('cy' + index),
               zoomingEnabled: false,
               boxSelectionEnabled: false,
               autounselectify: true,
               panningEnabled: true,
               zoom: 0.7,

               style: cytoscape.stylesheet()
                 .selector('node')
                   .css({
                     'content': 'data(id)'
                   })
                 .selector('edge')
                   .css({
                     'curve-style': 'bezier',
                     'target-arrow-shape': 'triangle',
                     'width': 4,
                     'line-color': '#ddd',
                     'target-arrow-color': '#ddd'
                   })
                 .selector('.highlighted')
                   .css({
                     'background-color': '#ff0000',
                     'line-color': '#ff0000',
                     'target-arrow-color': '#ff0000',
                     'transition-property': 'background-color, line-color, target-arrow-color',
                     'transition-duration': '0.5s'
                   })
                .selector('.api')
                   .css({
                     'background-color': '#ff0000',
                     'line-color': '#11aaff',
                     'target-arrow-color': '#11aaff',
                     'transition-property': 'background-color, line-color, target-arrow-color',
                     'transition-duration': '0.5s'
                   }),

               elements: {
                   nodes: [
                     { data: { id: 'a' } },
                     { data: { id: 'b' } },
                     { data: { id: 'c' } },
                     { data: { id: 'd' } },
                     { data: { id: 'e' } },
                     { data: { id: 'f' } },
                     { data: { id: 'g' } },
                     { data: { id: 'h' } },
                     { data: { id: 'i' } }
                   ],

                   edges: [
                     { data: { id: 'ab', source: 'a', target: 'b', kind: 'api' } },
                     { data: { id: 'ac', source: 'a', target: 'c' , kind: 'implementation' } },
                     { data: { id: 'bd', source: 'b', target: 'd' , kind: 'implementation' } },
                     { data: { id: 'be', source: 'b', target: 'e' , kind: 'api' } },
                     { data: { id: 'cf', source: 'c', target: 'f' , kind: 'implementation' } },
                     { data: { id: 'cg', source: 'c', target: 'g' , kind: 'api' } },
                     { data: { id: 'eh', source: 'e', target: 'h' , kind: 'api' } },
                     { data: { id: 'gh', source: 'g', target: 'h' , kind: 'implementation' } },
                     { data: { id: 'hi', source: 'h', target: 'i' , kind: 'implementation' } }
                   ]
                 },

               layout: {
                 name: 'breadthfirst',
                 directed: true,
                 roots: '#a',
                 padding: 5
               }
          });

          cy.panBy({x:50, y:80});
          
          fun(cy);
          
          });
    });
}
