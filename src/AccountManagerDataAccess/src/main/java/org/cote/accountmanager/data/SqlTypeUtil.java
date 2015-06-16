/*******************************************************************************
 * Copyright (C) 2002, 2015 Stephen Cote Enterprises, LLC. All rights reserved.
 * Redistribution without modification is permitted provided the following conditions are met:
 *
 *    1. Redistribution may not deviate from the original distribution,
 *        and must reproduce the above copyright notice, this list of conditions
 *        and the following disclaimer in the documentation and/or other materials
 *        provided with the distribution.
 *    2. Products may be derived from this software.
 *    3. Redistributions of any form whatsoever must retain the following acknowledgment:
 *        "This product includes software developed by Stephen Cote Enterprises, LLC"
 *
 * THIS SOFTWARE IS PROVIDED BY STEPHEN COTE ENTERPRISES, LLC ``AS IS''
 * AND ANY EXPRESSED OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO,
 * THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR
 * PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THIS PROJECT OR ITS CONTRIBUTORS
 * BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
 * DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS
 * OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION)
 * HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, 
 * STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY 
 * OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *******************************************************************************/
package org.cote.accountmanager.data;

import java.sql.Types;
import java.util.Date;

import org.apache.log4j.Logger;
import org.cote.accountmanager.objects.types.SqlDataEnumType;

public class SqlTypeUtil {
	public static final Logger logger = Logger.getLogger(SqlTypeUtil.class.getName());
	public static SqlDataEnumType translateSqlType(DBFactory.CONNECTION_TYPE connectionType, String dataType){
		
		SqlDataEnumType out_type = SqlDataEnumType.NULL;
		switch(connectionType){
			case POSTGRES:
				if(dataType.equals("int8") || dataType.equals("long") || dataType.equals("bigserial")){
					out_type = SqlDataEnumType.BIGINT;
				}				
				else if(dataType.startsWith("int") || dataType.equals("serial")){
					out_type = SqlDataEnumType.INTEGER;
				}
				else if(dataType.equals("bool")){
					out_type = SqlDataEnumType.BOOLEAN;
				}
				else if(dataType.equals("bytea")){
					out_type = SqlDataEnumType.BLOB;
				}
				else if(dataType.equals("float8")){
					out_type = SqlDataEnumType.DOUBLE;
				}

				else{
					//System.out.println("Unhandled data type: '" + dataType + "'");
					//logger.error("Unhandled data type: '" + dataType + "'");
				}

				break;
			default:
				System.out.println("Unhandled Type: " + connectionType);
				break;
		}
		if(out_type == SqlDataEnumType.NULL){
			try{
				out_type = SqlDataEnumType.valueOf(dataType.toUpperCase());
			}
			catch(Exception e){
				System.out.println(e.getMessage());
			}
		}
		return out_type;
	}
	public static <T> SqlDataEnumType getType(T obj){
		SqlDataEnumType out_type = SqlDataEnumType.NULL;
		if(obj instanceof String){
			out_type = SqlDataEnumType.VARCHAR;
		}
		else if(obj instanceof Integer){
			out_type = SqlDataEnumType.INTEGER;
		}
		else if(obj instanceof Long){
			out_type = SqlDataEnumType.BIGINT;
		}
		return out_type;
	}
	/*
	public static Class Type(SqlDataEnumType dataType){
		Class out_type = Object.class;
		switch(dataType){
			case BINARY:
				out_type = byte[].class;
				break;
			case VARCHAR:
				out_type = String.class;
				break;
			case INTEGER:
				out_type = int.class;
				break;
			case DATE:
			case TIMESTAMP:
			case TIME:
				out_type = Date.class;
				break;
		}
		return out_type;
	}
	*/
	public static int getSqlType(StatementParameter param){
		int out_type = Types.NULL;
		if(param == null || param.getParameterType() == null) return out_type;
		switch(param.getParameterType()){
			case ARRAY:
				out_type = 2003;
				break;
			case BIGINT:
				out_type = -5;
				break;

			case BINARY:
				out_type = -2;
				break;
			case BIT:
				out_type = -7;
				break;
			case BLOB:
				out_type = 2004;
				break;

			case BOOLEAN:
				out_type = 16;
				break;
			case CHAR:
				out_type = 1;
				break;
			case CLOB:
				out_type = 2005;
				break;

			case DATALINK:
				out_type = 70;
				break;
			case DATE:
				out_type = 91;
				break;
			case DECIMAL:
				out_type = 3;
				break;

			case DISTINCT:
				out_type = 2001;
				break;
			case DOUBLE:
				out_type = 8;
				break;
			case FLOAT:
				out_type = 6;
				break;

			case INTEGER:
				out_type = 4;
				break;
				
			case JAVA_OBJECT:
				out_type = 2004;
				break;
			case LONGVARBINARY:
				out_type = -4;
				break;

			case LONGVARCHAR:
				out_type = -1;
				break;
			case NULL:
				out_type = 0;
				break;
			case NUMERIC:
				out_type = 2;
				break;

			case OTHER:
				out_type = 1111;
				break;
			case REAL:
				out_type = 7;
				break;
			case REF:
				out_type = 2006;
				break;

			case SMALLINT:
				out_type = 5;
				break;
			case STRUCT:
				out_type = 2002;
				break;
			case TIME:
				out_type = 92;
				break;

			case TIMESTAMP:
				out_type = 93;
				break;
			case TINYINT:
				out_type = -6;
				break;
			case VARBINARY:
				out_type = -3;
				break;

			case VARCHAR:
				out_type = 12;
				break;				
		}
		return out_type;
	}
}
