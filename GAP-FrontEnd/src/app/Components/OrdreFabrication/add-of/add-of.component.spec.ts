import { ComponentFixture, TestBed } from '@angular/core/testing';

import { AddOFComponent } from './add-of.component';

describe('AddOFComponent', () => {
  let component: AddOFComponent;
  let fixture: ComponentFixture<AddOFComponent>;

  beforeEach(() => {
    TestBed.configureTestingModule({
      declarations: [AddOFComponent]
    });
    fixture = TestBed.createComponent(AddOFComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
