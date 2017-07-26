var namespace_info_table_DataTable, namespace_zkcluster_manager_table_DataTable;
$(function() {
	
	$("#refresh_registry_center").on("click", function() {
		var $btn = $(this).button("loading");
		$.get("registry_center/refreshRegCenter", function(data) {
			if(data.success == true) {
				window.parent.location.reload(true);
			} else {
				showFailureDialogWithMsg("failure-dialog", data.message);
			}
		}).always(function() { $btn.button('reset'); });
	});
	
	$('[href="#namespace_info_tabpanel"]').on("click", function() {
		refreshNamespaceInfo();
    });
	$('[href="#zk_cluster_info_tabpanel"]').on("click", function() {
		refreshZkClusterInfo();
	});
	$('[href="#namespace_zkcluster_manager_tabpanel"]').on("click", function() {
		refreshNamespaceZkCluster();
    });
	if ($("#namespace_info_tabpanel").is(':visible')) {
		refreshNamespaceInfo();
	} else if ($("#zk_cluster_info_tabpanel").is(':visible')) {
		refreshZkClusterInfo();
	} else if ($("#namespace_zkcluster_manager_tabpanel").is(':visible')) {
		refreshNamespaceZkCluster();
	}
	
});

function refreshNamespaceInfo() {
	$.get("registry_center/getNamespaceInfo", {}, function(data) {
		if(namespace_info_table_DataTable) {
			namespace_info_table_DataTable.destroy();
		}
        $("#namespace_info_table tbody").empty();
        if(data.success) {
    		var list = data.obj;
	        if(list && list instanceof Array) {
	            for (var i = 0;i < list.length;i++) {
            		var cluster = list[i];
            		var key = cluster.key;
            		var regCenterConfList = cluster.regCenterConfList;
            		if(regCenterConfList && regCenterConfList instanceof Array) {
            			for(var j=0; j < regCenterConfList.length; j++) {
	        				var regCenterConf = regCenterConfList[j];
			                var baseTd = "<td><a href='overview?name=" + regCenterConf.name + "/" + regCenterConf.namespace + "'>" + regCenterConf.namespace + "</a></td><td>"
			                    + regCenterConf.name + "</td><td>" + regCenterConf.version + "</td><td>" + key + "</td>";
			                $("#namespace_info_table tbody").append("<tr>" + baseTd + "</tr>");
            			}
            		}
	            }
	        }
	        namespace_info_table_DataTable = $("#namespace_info_table").DataTable({"oLanguage": language, "displayLength":100});
        } else {
    		showFailureDialogWithMsg("failure-dialog", data.message);
        }
    });
}

function refreshNamespaceZkCluster() {
	$.get("registry_center/getNamespaceZkclusterMappingList", {}, function(data) {
		if(namespace_zkcluster_manager_table_DataTable) {
			namespace_zkcluster_manager_table_DataTable.destroy();
		}
        $("#namespace_zkcluster_manager_table tbody").empty();
        if(data.success) {
    		var list = data.obj;
	        if(list && list instanceof Array) {
	            for (var i = 0;i < list.length;i++) {
            		var cluster = list[i];
            		var key = cluster.key;
            		var regCenterConfList = cluster.regCenterConfList;
            		if(regCenterConfList && regCenterConfList instanceof Array) {
            			for(var j=0; j < regCenterConfList.length; j++) {
            				var regCenterConf = regCenterConfList[j];
			                var baseTd = "<td><a href='overview?name=" + regCenterConf.name + "/" + regCenterConf.namespace + "'>" + regCenterConf.namespace + "</a></td><td>"
			                    + regCenterConf.name + "</td><td>" + regCenterConf.version + "</td><td>" + key + "</td>";
			                $("#namespace_zkcluster_manager_table tbody").append("<tr>" + baseTd + "</tr>");
            			}
            		}
	            }
	        }
	        namespace_zkcluster_manager_table_DataTable = $("#namespace_zkcluster_manager_table").DataTable({"oLanguage": language, "displayLength":100});
        } else {
            showFailureDialogWithMsg("failure-dialog", data.message);
        }
    });
}

function refreshZkClusterInfo() {
	
}
