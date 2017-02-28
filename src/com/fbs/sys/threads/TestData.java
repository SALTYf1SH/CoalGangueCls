package com.fbs.sys.threads;

import java.io.File;
import java.sql.SQLException;
import java.util.Date;

import com.fbs.calcu.CalcuTool;
import com.fbs.sys.Controlor;
import com.fbs.sys.dao.SQLDao;
import com.fbs.sys.dtos.ReadImageDto;
import com.fbs.sys.tools.Operations;
import com.fbs.sys.uis.MainFrame;

public class TestData implements Runnable {
	private boolean testrun=false;

	public TestData() {
	}

	public void stopRun(){
		testrun=false;
	}
	
	@Override
	public void run() {
		
		SQLDao dao = new SQLDao();
		dao.getConnect();
		
		Operations operation=Operations.getInstance();
		MainFrame.showMessage("�ļ�·����"+operation.getImageDir());
		MainFrame.showMessage("��ֵ�˲����ڣ�"+operation.getFilterWidth()+
				"*"+operation.getFilterWidth());
		MainFrame.showMessage("��ֵ����ֵ��"+operation.getThreshold());
		MainFrame.showMessage("");
		MainFrame.showMessage("���ڼ���...\n");
		
		int i;
		ReadImageDto img=null;
		CalcuTool culcu=null;
		long time;
		testrun=true;
		for(i=0; i<
				new File(operation.getImageDir()).list().length && testrun;i++){
				img=operation.readImage((i+1)+".png");
				culcu=operation.testData(img);
				if(culcu == null){
					MainFrame.showMessage(img.getNum()+"δ��⵽����");
					dao.insertCoalTest(img.getNum(), 0, 0, 0);
					continue;
				}
				MainFrame.showMessage(img.getNum()+"*��ֵ*"+culcu.getAverage()+
						" *��������*"+culcu.getSampleVariance());
				time=(new Date().getTime())-img.getTime();
				MainFrame.showMessage("��ʱ��"+time+"ms");
				MainFrame.showMessage("");
				
				dao.insertCoalTest(img.getNum(),
						culcu.getAverage(), culcu.getSampleVariance(), time);
		}
		MainFrame.showMessage("�߳�testdata������");
		Controlor.getInstance().testStop();
		try {
			dao.CutConnection();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
}
