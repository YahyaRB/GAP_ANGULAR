import { ComponentFixture, TestBed } from '@angular/core/testing';

import { UpdateNomenclatureComponent } from './update-nomenclature.component';

describe('UpdateNomenclatureComponent', () => {
  let component: UpdateNomenclatureComponent;
  let fixture: ComponentFixture<UpdateNomenclatureComponent>;

  beforeEach(() => {
    TestBed.configureTestingModule({
      declarations: [UpdateNomenclatureComponent]
    });
    fixture = TestBed.createComponent(UpdateNomenclatureComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
