package com.fbs.image;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.Queue;

public class GrayScaleTools {
	/*
	 * 1 ת8bit�Ҷ�ͼ
	 * 
	 * ��ȡ�Ҷ�ͼ�����imageDto
	 * 8bit�Ҷ�ͼ��
	 * 2 ��ֵ�˲�
	 * 3 ת��ֵͼ
	 * 4 ��Ե׷��
	 * 5 �������
	 * 6 �õ���������Ӧԭͼ�Ҷ�
	 * */
	//private GrayScaleDto image=null;
	
	/* ��8bit�Ҷ�ͼ������GrayScaleDto */
	static public GrayScaleDto toGrayScaleDto(final BufferedImage image){
		int[] pixels=new int[image.getWidth()*image.getHeight()];
		int x,y;
		for(y=0; y<image.getHeight(); y++)
			for(x=0; x<image.getWidth(); x++)
				pixels[y*image.getWidth()+x]=image.getRGB(x,y)&0xff;
		return new GrayScaleDto(pixels,image.getWidth(),image.getHeight());
	}
	
	/* ��ֵ�˲� �˲���:filterWidth*filterWidth */
	private static int getMedValue(int[] arr,int width){
		Arrays.sort(arr);
		return arr[(int) Math.floor(width*width/2)];
	}

	public static GrayScaleDto medianFilter(final GrayScaleDto origeImage,final int filterWidth){
		int[] filter=new int[filterWidth*filterWidth];
		// ��ȸ��ƣ�origeImage���ᱻ�ı�
		int[] oldImage=new int[origeImage.getImage().length];
		System.arraycopy(origeImage.getImage(), 0,
				oldImage, 0, origeImage.getImage().length);
		// ��ͼ ȥ����Ե��
		int[] newImage=new int[(origeImage.getWidth())
		                 *(origeImage.getHeight())];
		Arrays.fill(newImage, 0x000000ff);	// ȫ��
		int y,x,fy,fx,cur,hfw=filterWidth/2;
		// ��Ե���ز�����
		for(y=0; y<origeImage.getHeight()-filterWidth; y++){
			for(x=0; x<origeImage.getWidth()-filterWidth; x++){
				cur=y*origeImage.getWidth()+x; //ѡ�����Ͻ�����,��ͼ��ǰ����

				// ��image�еĴ�������filter��
				for(fy=0; fy<filterWidth; fy++) // y
					for(fx=0; fx<filterWidth; fx++) // x
						filter[fy*filterWidth+fx]=oldImage[cur+fy*origeImage.getWidth()+fx];
				//��ԭͼ�� ������� ���ĵ� ���м�ֵ
				oldImage[cur+hfw*origeImage.getWidth()+hfw]=getMedValue(filter,filterWidth);
				//�������ĵ㸳����ͼ
				newImage[cur+hfw*origeImage.getWidth()+hfw]=oldImage[cur+hfw*origeImage.getWidth()+hfw];
			}
		}
		return new GrayScaleDto(newImage,
				origeImage.getWidth(),origeImage.getHeight());
	}

	/* ���ɶ�ֵͼ */
	public static GrayScaleDto getBinaryImage(final GrayScaleDto image,
			final int threshold,final int mincount){
		int y,x,count=0;
		int[] oldImage=image.getImage();
		int[] newImage=new int[oldImage.length];
		for(y=0; y<image.getHeight(); y++)
			for(x=0; x<image.getWidth(); x++)
				/*	�Ҷ�=0xff ��ɫ���Ҷ�=0 ��ɫ
				 *	�����ɫ��ȡȫ1������ȡ0
				 */
				if(oldImage[y*image.getWidth()+x] < threshold){
					newImage[y*image.getWidth()+x]=0;
					count++;
				}
				else
					newImage[y*image.getWidth()+x]=0xff;
		if(count<mincount)
			return null;
		return new GrayScaleDto(newImage,image.getWidth(),image.getHeight());
	}
	
	/* ��Ե��1 ��Ե����>=mincount
	 * ������ֵ�˲�ͼƬ������һȦ����һ��Ϊ0xff
	 */
	public static EdgeImageDto getEdge(final GrayScaleDto image,final int mincount){
		int beginloc=0;
		EdgeImageDto edge=null;
		while(beginloc <= (image.getImage().length-mincount)){
			edge=getEdgeFromLoc(image,beginloc);
			if(edge == null)
				break;
			if(edge.getCount() >= mincount)
				return edge;
			beginloc+=image.getWidth(); // ��һ�п�ʼ �����Ż�
		}
		return null;
	}

	/* ��beginloc��ʼ ��Ե��1
	 *������ֵ�˲�ͼƬ������һȦ����һ��Ϊ0xff
	 * ��Ե׷��,��Ե��-1�����ַ�������Ϊ��ɫ������Ϊ��ɫ���
	 */
	private static EdgeImageDto getEdgeFromLoc(final GrayScaleDto image,final int beginloc){
		int[] oldImage=image.getImage();
		// ��ǵı�Ե����,��ʼ��Ϊ0xff,�ҵ���Ե��0
		int[] marked=new int[image.getHeight()*image.getWidth()];
		Arrays.fill(marked, 0xff);
		/* count�����Ե���ȣ�
		*��С��ĳֵ�������first+1������ʼ�Ĳ���
		*ֱ��ͼƬ������ 
		*/
		final int[] wwalk={image.getWidth()-1,image.getWidth(),image.getWidth()+1,1,
				1-image.getWidth(),-image.getWidth(),-1-image.getWidth(),-1};
		int first=beginloc,prewhite,curblack,count=0;
		int wdirector=0;

		/* �ҵ�ĳ������Ե��λ��,0�Ǻ�ɫ */
		while(first<oldImage.length && oldImage[first++] != 0)
			;
		// ͼƬ��ľ������(����)
		if(first == marked.length)
			return null;
		first--;
		// ����һ����Ե��0
		marked[first]=0;
		curblack=first;
		prewhite=first-1;
		wdirector=0;
		int c=0;
		do{
			// ���ַ��� �׵��˶�����Ϊ��ʱ��
			if(prewhite+image.getWidth() == curblack){
				// ���Ϻ��£���Ժڣ��׵ķ��� ����
				wdirector=6;
			}else if(prewhite+1 == curblack){
				// ������ң���Ժڣ��׵ķ��� ����
				wdirector=0;
			}else if(prewhite-1 == curblack){
				// ���Һ�����Ժڣ��׵ķ��� ����
				wdirector=4;
			}else if(prewhite-image.getWidth() == curblack){
				// ���º��ϣ���Ժڣ��׵ķ��� ����
				wdirector=2;
			}
			c=0;
			while(oldImage[curblack+wwalk[wdirector]] != 0 && c<9){
				// δ������ɫ��Ե
				prewhite=curblack+wwalk[wdirector];
				wdirector=(wdirector+1)%8;
				c++;
			}
			if(c == 8)
				break;
			curblack+=wwalk[wdirector];
			count++;
			marked[curblack]=0; // ��Ե=0
		}while(curblack != first);
		return new EdgeImageDto(marked,image.getWidth(),image.getHeight(),count);
	}

	/* ��Եͼ�õ� ԭͼ ��Ե�ڲ���Ŀ������ 
	 * ������ֵ�˲���ͼ����ΧһȦһ����0xff
	 * ���㷨���ص�Ŀ�겻������Ե
	 * Ŀ������=0xff
	 */
	public static GrayScaleDto getMarkedImage(final EdgeImageDto edge){
		// Ŀ����������
		int[] target=new int[edge.getWidth()*edge.getHeight()];
		// review:mark[]��ʼ��Ϊ0xff ��Ե������0
		System.arraycopy(edge.getMark(),0,target,0,target.length);
		// ������ �� �� ��
		final int[] directions={-edge.getWidth(),1,edge.getWidth(),-1};
		/* ��0ֱ������0,���µ�ͼ��ΪĿ������
		 * willview��Ŵ����������±�
		 */ 
		int cur=0,i=0; // ��ǰ�����±�
		Queue<Integer> willview=new LinkedList<Integer>();
		target[cur]=0;
		willview.add(0);
		while(!willview.isEmpty()){
			cur=willview.poll();
			i=0;
			while(i<4){
				int tem=cur+directions[i];
				if(tem<target.length && tem>0 && target[tem] != 0){
					// δ�����ʹ� ���ʸ�0
					target[tem]=0;
					// ��� �� �����ٽ����� �����ص��±�
					willview.add(tem);
				}
				i++;
			}
		}
		return new GrayScaleDto(target,edge.getWidth(),edge.getHeight());
	}

	/* �ɱ��ͼ�ͻҶ�ͼ �õ�������� int[]�Ҷ�ֵ����
	 */
	public static int[] getObjectImage(final GrayScaleDto image,
			final GrayScaleDto marked){
		int[] grays=new int[256]; // ��ʼ��Ĭ��0
		int[] oriImage=image.getImage(),objMark=marked.getImage();
		int cur;
		for(cur=0; cur<image.getImage().length; cur++){
			if(objMark[cur] != 0) // ��ȥ����������markѡ�е�����
				++(grays[ (oriImage[cur] & objMark[cur]) ]);
		}
		return grays;
	}

	public static BufferedImage toBufferedImage(final GrayScaleDto image){
		 BufferedImage grayImage = 
				new BufferedImage(image.getWidth(),image.getHeight(),
							BufferedImage.TYPE_BYTE_GRAY);
		 int i=0;
		 int[] img=image.getImage();
		 int[] bimg=new int[img.length];
		 while(i < img.length){
			 bimg[i]=new Color(img[i],img[i],img[i]).getRGB();
			 i++;
		 }
		grayImage.setRGB(0, 0, image.getWidth(), 
			image.getHeight(), bimg, 0, image.getWidth());
		 return grayImage;
	}

	public static int[] getGrays(GrayScaleDto dto,int fw,int ts,int mcb,int mce){
		GrayScaleDto img=GrayScaleTools.medianFilter(dto,fw);
		
		// Ŀ������Ҳ���
		GrayScaleDto bin=GrayScaleTools.getBinaryImage(img, ts, mcb);
		if(bin == null)
			return null;
		
		// ��Ե�п����Ҳ���
		EdgeImageDto edge=GrayScaleTools.getEdge(bin, mce);
		if(edge == null)
			return null;
		
		GrayScaleDto objmark=GrayScaleTools.getMarkedImage(edge);
		
		return GrayScaleTools.getObjectImage(img, objmark);
	}
	
	/*private static String showXY(int loc,int width){
		int x=loc%width;
		int y=loc/width;
		return new String("("+x+","+y+")");
	}*/
	
	public static GrayScaleDto getObj(final GrayScaleDto image,
						final GrayScaleDto mark){
		int i=0;
		int[] o=image.getImage();
		int[] m=mark.getImage();
		int[] newimage=new int[o.length];
		while(i<m.length){
			newimage[i]=o[i] & m[i];
			i++;
		}
		return new GrayScaleDto(newimage,image.getWidth(),image.getHeight());
	}
}
