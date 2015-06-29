<#import "*/layout/defaultlayout.ftl" as layout>
<@layout.vaultLayout>
    <#import "/spring.ftl" as spring />
<div class="container">

            <h1>Withdraw</h1>

            <form class="form" role="form" action="" method="post">

                <div class="form-group">
                    <label class="control-label">Filepath:</label>
                    <@spring.bind "withdrawal.withdrawalPath" />
                    <input type="text"
                            class="form-control file-path"
                            name="${spring.status.expression}"
                            value="${spring.status.value!""}"/>
                </div>

                <div id="tree" class="fancytree-radio tree-box"></div>

                <script>
                    // Create the tree inside the <div id="tree"> element.
                    $("#tree").fancytree({
                        source: {
                                url: "/datavault-webapp/files",
                                cache: false
                        },
                        lazyLoad: function(event, data){
                            var node = data.node;
                            // Load child nodes via ajax GET /files?mode=children&parent=1234
                            data.result = {
                                url: "/datavault-webapp/files",
                                data: {mode: "children", parent: node.key},
                                cache: false
                            };
                        },
                        checkbox: true,
                        selectMode: 1,
                        select: function(event, data) {
                            var nodes = data.tree.getSelectedNodes();
                            $(".file-path").val("");
                            nodes.forEach(function(node) {
                                $(".file-path").val(node.key);
                            });
                        }
                    });
                </script>

                <div>
                    <button type="submit" class="btn btn-primary">Submit</button>
                </div>


            </form>

</div>
</@layout.vaultLayout>