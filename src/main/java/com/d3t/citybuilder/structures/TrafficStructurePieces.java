package com.d3t.citybuilder.structures;

public class TrafficStructurePieces {

	public Structure dot;
	
	public Structure end_N;
	public Structure end_E;
	public Structure end_S;
	public Structure end_W;
	
	public Structure straight_NS;
	public Structure straight_WE;
	
	public Structure curve_NE;
	public Structure curve_ES;
	public Structure curve_SW;
	public Structure curve_WN;
	
	public Structure crossT_N;
	public Structure crossT_E;
	public Structure crossT_S;
	public Structure crossT_W;
	
	public Structure crossX;
	
	//Connections: 4 char string (NESW)
	public Structure getStructureForChunk(String connections) {
		switch(connections) {
			case "0000": return getPiece(dot);
			
			case "1000": return getPiece(end_N);
			case "0100": return getPiece(end_E);
			case "0010": return getPiece(end_S);
			case "0001": return getPiece(end_W);
			
			case "1010": return getPiece(straight_NS);
			case "0101": return getPiece(straight_WE);
			
			case "1100": return getPiece(curve_NE);
			case "0110": return getPiece(curve_ES);
			case "0011": return getPiece(curve_SW);
			case "1001": return getPiece(curve_WN);
			
			case "1101": return getPiece(crossT_N);
			case "1110": return getPiece(crossT_E);
			case "0111": return getPiece(crossT_S);
			case "1011": return getPiece(crossT_W);
			
			case "1111": return getPiece(crossX);
			
			default: return getPiece(dot);
		}
	}
	
	private Structure getPiece(Structure piece) {
		if(piece == null) piece = crossX;
		return piece;
	}
}
