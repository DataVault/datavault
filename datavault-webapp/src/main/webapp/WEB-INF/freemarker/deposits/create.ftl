<#import "*/layout/defaultlayout.ftl" as layout>
<@layout.vaultLayout>
    <#import "/spring.ftl" as spring />
<div class="container">
    <div class="row">
        <div class="col-xs-12 storage">
            <h1>Create New Deposit</h1>

            <form class="form" role="form" action="" method="post">

                <div class="form-group">
                    <label class="control-label">Deposit Note:</label>
                    <@spring.bind "deposit.note" />
                    <input type="text"
                           class="form-control"
                           name="${spring.status.expression}"
                           value="${spring.status.value!""}"/>
                </div>

                <div class="form-group">
                    <label class="control-label">Filepath:</label>
                    <@spring.bind "deposit.filePath" />
                    <textarea type="text" name="${spring.status.expression}"
                              class="form-control"
                              value="${spring.status.value!""}" rows="6" cols="60"></textarea>
                    </div>
                </div>

                <div class="form-group">
                    <div id="tree" class="fancytree-radio">
                      <ul id="treeData" style="display: none;">
                        <li id="id1">File 1
                        <li id="id2">File 2
                        <li id="id3" class="folder">Folder 3
                          <ul>
                            <li id="id3.1">File 3.1
                            <li id="id3.2">File 3.2
                         </ul>
                      </ul>
                    </div>
                </div>

                <script type="text/javascript">
                  $(function(){
                    $("#tree").fancytree({
                        checkbox: true,
                        selectMode: 1
                    });
                  });
                </script>

                <div class="modal-footer">
                    <input type="submit" class="btn btn-primary" value="Submit"/>
                </div>

            </form>
        </div>
    </div>
</div>
</@layout.vaultLayout>