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
import { Component, OnInit, OnDestroy, Input, Output, EventEmitter } from '@angular/core';
import { AttributeGroupVO } from './../../../shared/model/index';
import { Futures, Future } from './../../../shared/event/index';
import { Config } from './../../../shared/config/env.config';
import { LogUtil } from './../../../shared/log/index';


@Component({
  selector: 'cw-attribute-groups',
  moduleId: module.id,
  templateUrl: 'attribute-groups.component.html',
})

export class AttributeGroupsComponent implements OnInit, OnDestroy {

  @Input() selectedGroup:AttributeGroupVO;

  @Output() dataSelected: EventEmitter<AttributeGroupVO> = new EventEmitter<AttributeGroupVO>();

  private _groups:Array<AttributeGroupVO> = [];
  private _filter:string;
  private delayedFiltering:Future;
  private delayedFilteringMs:number = Config.UI_INPUT_DELAY;

  private filteredGroups:Array<AttributeGroupVO>;

  //paging
  private maxSize:number = Config.UI_TABLE_PAGE_NUMS; // tslint:disable-line:no-unused-variable
  private itemsPerPage:number = Config.UI_TABLE_PAGE_SIZE;
  private totalItems:number = 0;
  private currentPage:number = 1; // tslint:disable-line:no-unused-variable
  // Must use separate variables (not currentPage) for table since that causes
  // cyclic even update and then exception https://github.com/angular/angular/issues/6005
  private pageStart:number = 0;
  private pageEnd:number = this.itemsPerPage;

  constructor() {
    LogUtil.debug('AttributeGroupsComponent constructed');
    let that = this;
    this.delayedFiltering = Futures.perpetual(function() {
      that.filterGroups();
    }, this.delayedFilteringMs);
  }

  ngOnInit() {
    LogUtil.debug('AttributeGroupsComponent ngOnInit');
  }

  @Input()
  set groups(groups:Array<AttributeGroupVO>) {
    this._groups = groups;
    this.filterGroups();
  }

  @Input()
  set filter(filter:string) {
    this._filter = filter ? filter.toLowerCase() : null;
    this.delayedFiltering.delay();
  }

  ngOnDestroy() {
    LogUtil.debug('AttributeGroupsComponent ngOnDestroy');
    this.selectedGroup = null;
    this.dataSelected.emit(null);
  }

  resetLastPageEnd() {
    let _pageEnd = this.pageStart + this.itemsPerPage;
    if (_pageEnd > this.totalItems) {
      this.pageEnd = this.totalItems;
    } else {
      this.pageEnd = _pageEnd;
    }
  }

  onPageChanged(event:any) {
    this.pageStart = (event.page - 1) * this.itemsPerPage;
    let _pageEnd = this.pageStart + this.itemsPerPage;
    if (_pageEnd > this.totalItems) {
      this.pageEnd = this.totalItems;
    } else {
      this.pageEnd = _pageEnd;
    }
  }

  protected onSelectRow(row:AttributeGroupVO) {
    LogUtil.debug('AttributeGroupsComponent onSelectRow handler', row);
    if (row == this.selectedGroup) {
      this.selectedGroup = null;
    } else {
      this.selectedGroup = row;
    }
    this.dataSelected.emit(this.selectedGroup);
  }

  private filterGroups() {
    if (this._filter) {
      this.filteredGroups = this._groups.filter(group =>
        group.code.toLowerCase().indexOf(this._filter) !== -1 ||
        group.name.toLowerCase().indexOf(this._filter) !== -1 ||
        group.description && group.description.toLowerCase().indexOf(this._filter) !== -1
      );
      LogUtil.debug('AttributeGroupsComponent filterGroups', this._filter);
    } else {
      this.filteredGroups = this._groups;
      LogUtil.debug('AttributeGroupsComponent filterGroups no filter');
    }

    if (this.filteredGroups === null) {
      this.filteredGroups = [];
    }

    let _total = this.filteredGroups.length;
    this.totalItems = _total;
    if (_total > 0) {
      this.resetLastPageEnd();
    }
  }

}
