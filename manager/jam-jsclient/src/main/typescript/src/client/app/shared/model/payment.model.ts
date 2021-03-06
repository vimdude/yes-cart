/*
 * Copyright 2009 Inspire-Software.com
 *
 *    Licensed under the Apache License, Version 2.0 (the 'License');
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an 'AS IS' BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */

export interface PaymentGatewayInfoVO {

  name : string;

  label : string;

  active : boolean;

}

export interface PaymentGatewayFeatureVO {

  supportAuthorize : boolean;
  supportCapture : boolean;
  supportAuthorizeCapture : boolean;
  supportVoid : boolean;
  supportReverseAuthorization : boolean;
  supportRefund : boolean;
  externalFormProcessing : boolean;
  onlineGateway : boolean;
  requireDetails : boolean;
  supportCaptureMore : boolean;
  supportCaptureLess : boolean;
  supportAuthorizePerShipment : boolean;
  additionalFeatures : string;

}

export interface PaymentGatewayParameterVO {

  paymentGatewayParameterId : number;
  description : string;
  label : string;
  name : string;
  value : string;
  pgLabel : string;
  businesstype : string;
  secure : boolean;

  createdTimestamp?:Date;
  updatedTimestamp?:Date;
  createdBy?:string;
  updatedBy?:string;

}

export interface PaymentGatewayVO extends PaymentGatewayInfoVO {

  rank : number;
  shopCode : string;
  feature : PaymentGatewayFeatureVO;
  parameters : Array<PaymentGatewayParameterVO>;

}
