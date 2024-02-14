package de.regasus.report.model;

import java.util.ArrayList;
import java.util.List;

import com.lambdalogic.messeinfo.report.data.UserReportDirVO;
import com.lambdalogic.messeinfo.report.data.UserReportVO;

public class UserReportTreeNode {

	private UserReportDirVO dir;
	private UserReportVO report;
	
	private UserReportTreeNode parent;
	private List<UserReportTreeNode> children;
	
	
	public UserReportTreeNode(UserReportDirVO userReportDirVO) {
		this.dir = userReportDirVO;
	}
	
	public UserReportTreeNode(UserReportVO userReportVO) {
		this.report = userReportVO;
	}
	
	
	public boolean isDir() {
		return dir != null;
	}

	
	public Object getData() {
		if (isDir()) {
			return dir;
		}
		else {
			return report;
		}
	}

	public UserReportTreeNode getParent() {
		return parent;
	}

	
	
	public List<UserReportTreeNode> getChildren() {
		return children;
	}

	
	public static List<UserReportTreeNode> generateTree(
		List<UserReportVO> userReportVOs, 
		List<UserReportDirVO> userReportDirVOs
	) {
		List<UserReportTreeNode> rootNodes = null;
		
		
		rootNodes = new ArrayList<UserReportTreeNode>(/* TODO: mit Anzahl der Wurzelknoten initialisieren*/);
		
		
		return rootNodes;
	}

}
