import { ComponentFixture, TestBed } from '@angular/core/testing';

import { AddNomenclatureComponent } from './add-nomenclature.component';

describe('AddNomenclatureComponent', () => {
  let component: AddNomenclatureComponent;
  let fixture: ComponentFixture<AddNomenclatureComponent>;

  beforeEach(() => {
    TestBed.configureTestingModule({
      declarations: [AddNomenclatureComponent]
    });
    fixture = TestBed.createComponent(AddNomenclatureComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
